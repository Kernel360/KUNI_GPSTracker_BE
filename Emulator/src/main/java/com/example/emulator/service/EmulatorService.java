package com.example.emulator.service;


import com.example.emulator.model.*;
import com.example.emulator.util.Car;
import com.example.emulator.util.CarRegistry;
import com.example.emulator.util.CarState;
import com.google.common.collect.Lists;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmulatorService {

    private final WebClient client;
    private final CarRegistry carRegistry;
    private final TokenService tokenService;
    private final ResourceLoader resourceLoader;

    /** 기본은 classpath:gps, 운영에서는 file:/data/gps 처럼 바꿔 쓰세요 */
    @Value("${emulator.gps.location:classpath:gps}")
    private String gpsLocationRoot;

    //전송 스트림 관리를 위한 Map
    private final ConcurrentMap<String, Disposable> running = new ConcurrentHashMap<>();
    //처음과 마지막 전송 데이터 저장
    private final ConcurrentMap<String, CSV> firstGps = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CSV> lastGps = new ConcurrentHashMap<>();

    //시간 보정 값 저장.
    private static final int MAX_PACKET_SIZE = 60;           // 사양상 한번에 60개씩 전송
    private final ConcurrentMap<String, Duration> timeShift = new ConcurrentHashMap<>();
    private static final DateTimeFormatter CSV_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 차량의 상태를 on/off 로 변경한다.
     * @param number
     * @param state
     */
    public void changeState(String number, String state) {
        Car car = carRegistry.get(number);
        car.setState(CarState.valueOf(state));
    }

    /** 차량 CSV 리소스 resolve */
    private Resource resolveCarCsv(String number) {
        return resourceLoader.getResource(gpsLocationRoot + "/" + number + ".csv");
    }

    /**
     * 차량에 대응되는 csv파일을 읽어서 interval 초 단위로 관제 서버로 데이터를 보내는 메서드
     * sendOnAndStart 메서드를 사용해서 ON 요청과 주기정보 요청을 보내는 메서드를 호출 한다.
     *
     * running에 현재 요청 스트림을 유지한다.
     *
     * @param csvResource
     * @param mdn
     * @param interval
     * @throws IOException
     */
    public void start(Resource csvResource, String mdn, int interval) throws IOException {
        stop(mdn);   // 같은 차량이 이미 돌고 있으면 중단

        Disposable d = sendOnAndStart(csvResource, mdn, interval)         //  /on 전송 →  interval 주기 /gps
                .doOnTerminate(() -> stop(mdn))
                .subscribe(
                        null,
                        err -> log.error("[{}] 스트림 오류: {}", mdn, err.toString())
                );

        running.put(mdn, d);
        changeState(mdn, "ON");
    }

    /**
     * 요청 스트림에서 차량 번호에 해당하는 스트림을 중단
     * @param mdn
     */
    public void stop(String mdn) {
        Disposable disposable = running.remove(mdn);

        if (disposable == null) {
            log.debug("[{}] 실행 중 스트림이 없어 stop()을 건너뜁니다", mdn);
            return;
        }

        disposable.dispose();

        changeState(mdn, "OFF");

        sendOff(mdn)
                .subscribe(
                        null,
                        err -> log.error("[{}] OFF 패킷 전송 실패: {}", mdn, err.getMessage()),
                        ()  -> log.info("[{}] OFF 패킷 전송 성공", mdn)
                );

        //유지 메모리 정리
        lastGps.remove(mdn);
        firstGps.remove(mdn);
    }

    /**
     * 관제 서버로 Off 요청 보내는 메서드
     *
     * @param mdn
     * @return
     */
    private Mono<Void> sendOff(String mdn) {

        CSV first = firstGps.get(mdn);
        CSV last = lastGps.get(mdn);

        if(last == null) last = first;

        if (last == null || first == null) {
            log.warn("[{}] first/last GPS 없음 → CarStop 생략", mdn);
            return Mono.empty();
        }

        DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String offTime = LocalDateTime.now().format(TS_FMT);
        String onTime = first.getTimestamp();

        Gps gps = Gps.builder()
                .gcd(last.getGcd())
                .lon(last.getLon())
                .spd(last.getSpd())
                .ang(last.getAng())
                .sum(last.getSum())
                .lat(last.getLat())
                .build();

        CarStopRequest req = CarStopRequest.builder()
                .mdn(mdn)
                .mdt(MDT.decidedMDT())
                .onTime(onTime)
                .offTime(offTime)
                .gps(gps)
                .build();

        return client.post()
                .uri("/off")
                .header("Authorization", tokenService.getToken(mdn))
                .bodyValue(req)     // 규격에 맞춰 수정
                .retrieve()
                .toBodilessEntity()
                .then();
    }

    /**
     * /on 전송 → 60초마다 /gps 전송
     * @param csvResource CSV 파일
     * @param mdn     차량 번호
     */
    public Mono<Void> sendOnAndStart(Resource csvResource, String mdn, int interval) throws IOException {

        return sendOn(csvResource, mdn)                 // 1. ON
                .then(sendInitialBurst(csvResource, mdn))// 2) 120개 즉시(60×2) 전송
                .flatMap(sentCount -> startGps(csvResource, mdn, interval, sentCount));
        //.then(startGps(csvResource, mdn, interval ,sentCount));  // 2. 주기 전송
    }

    private Mono<Integer> sendInitialBurst(Resource csvResource, String mdn) {
        Mono<List<CSV>> listMono = Mono.fromCallable(() -> loadCsv(csvResource))
                .subscribeOn(Schedulers.boundedElastic());

        return listMono.flatMap(all -> {
            // 최대 120개만 즉시 전송
            int limit = Math.min(120, all.size());
            if (limit <= 0) {
                log.warn("[{}] 초기 버스트: CSV 비어 있음 → 전송 생략", mdn);
                return Mono.just(0);
            }

            List<CSV> first120 = all.subList(0, limit);
            List<List<CSV>> chunks = Lists.partition(first120, MAX_PACKET_SIZE); // 60개씩

            LocalDateTime base = LocalDateTime.now(); // on 직후 시각
            DateTimeFormatter OUT_MIN_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

            List<Mono<Void>> sends = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                // i=0 → now-2분, i=1 → now-1분
                LocalDateTime minute = base.minusMinutes(2 - i);
                String oTime = minute.truncatedTo(ChronoUnit.MINUTES).format(OUT_MIN_FMT);
                sends.add(sendGpsBatchWithOTime(mdn, chunks.get(i), oTime));
            }

            // lastGps 갱신
            lastGps.put(mdn, first120.get(first120.size() - 1));

            return Mono.when(sends).thenReturn(limit); // 보낸 개수 반환
        });
    }

    private Mono<Void> sendGpsBatchWithOTime(String mdn, List<CSV> batch, String oTime) {
        AtomicInteger idx = new AtomicInteger(0);

        List<MdtGpsRequest.CList> cList = batch.stream()
                .map(csv -> MdtGpsRequest.CList.builder()
                        .sec(String.format("%02d", idx.getAndIncrement()))
                        .bat(csv.getBat())
                        .gps(Gps.builder()
                                .gcd(csv.getGcd()).lat(csv.getLat()).lon(csv.getLon())
                                .ang(csv.getAng()).spd(csv.getSpd()).sum(csv.getSum())
                                .build())
                        .build())
                .toList();

        MdtGpsRequest req = MdtGpsRequest.builder()
                .mdn(mdn).mdt(MDT.decidedMDT()).oTime(oTime)
                .cCnt(String.valueOf(cList.size())).cList(cList).build();

        log.info("[{}] 초기 버스트 GPS 전송 ({}건) oTime={}", mdn, batch.size(), oTime);

        return client.post().uri("/gps")
                .header("Authorization", tokenService.getToken(mdn))
                .bodyValue(req).retrieve().toBodilessEntity().then();
    }

    /**
     * 관제 서버로 On 요청을 보내느 메서드
     * @param csvResource
     * @param number
     * @return
     * @throws IOException
     */
    public Mono<Void> sendOn(Resource csvResource, String number) throws IOException {

        List<CSV> all = loadCsv(csvResource);
        CSV csvFirst = all.get(0);

//        // 첫 타임스탬프를 '지금'으로 평행이동시킬 Δt
//        Duration delta = Duration.between(
//                LocalDateTime.parse(csvFirst.getTimestamp(), CSV_FMT),
//                LocalDateTime.now()
//        );
//        timeShift.put(number, delta);
//
//        DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
//        String onTime = LocalDateTime.now().format(TS_FMT);

        // ✅ ON 직후 '초기 버스트'가 now-2, now-1로 나가므로
        //    timeShift도 'now-2분'을 기준으로 계산해야 이후(121번째)가 정확히 'now'가 됨.
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime alignBase = now.minusMinutes(2); // 초기 버스트의 시작 기준
        Duration delta = Duration.between(
                LocalDateTime.parse(csvFirst.getTimestamp(), CSV_FMT),
                alignBase
        );
        timeShift.put(number, delta);

        // onTime은 프로토콜상 '지금' 사용
        DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String onTime = now.format(TS_FMT);

        csvFirst.setTimestamp(onTime);
        firstGps.put(number, csvFirst);

        Gps gps = Gps.builder()
                .gcd(csvFirst.getGcd())
                .lon(csvFirst.getLon())
                .spd(csvFirst.getSpd())
                .ang(csvFirst.getAng())
                .sum(csvFirst.getSum())
                .lat(csvFirst.getLat())
                .build();

        CarStartRequest req = CarStartRequest.builder()
                .mdn(number)
                .mdt(MDT.decidedMDT())
                .onTime(onTime)
                .offTime(null)
                .gps(gps)
                .build();

        log.info("on에서 보내는 것 : {}",req);

        return client.post()
                .uri("/on")
                .header("Authorization", tokenService.getToken(number))
                .bodyValue(req)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(v -> log.info("[{}] ON 전송 성공", number))
                .doOnError(e -> log.error("[{}] ON 전송 실패: {}", number, e.getMessage()))
                .then();
    }

    /**
     * CSV 파일을 읽어서 gps 정보를 리스트로 반환한다.
     *
     * @param csvResource
     * @return
     * @throws IOException
     */
//    public List<CSV> loadCsv(Path csvPath) throws IOException {
//        try (Reader reader = Files.newBufferedReader(csvPath)) {
//
//            var csvToBean = new CsvToBeanBuilder<CSV>(reader)
//                    .withType(CSV.class)
//                    .withIgnoreLeadingWhiteSpace(true) // 앞뒤 공백 무시
//                    .withSeparator(',')
//                    .withSkipLines(0)
//                    .build();
//
//            return csvToBean.parse();
//        }
//    }
    public List<CSV> loadCsv(Resource csvResource) throws IOException {
        if (!csvResource.exists()) {
            throw new IOException("CSV not found: " + csvResource);
        }
        try (Reader reader = new BufferedReader(
                new InputStreamReader(csvResource.getInputStream(), StandardCharsets.UTF_8))) {

            var csvToBean = new CsvToBeanBuilder<CSV>(reader)
                    .withType(CSV.class)
                    .withIgnoreLeadingWhiteSpace(true) // 앞뒤 공백 무시
                    .withSeparator(',')
                    .withSkipLines(0)
                    .build();

            return csvToBean.parse();
        }
    }

    /** CSV를 한 번 읽고 interval초 간격으로 전송 */
    private Mono<Void> startGps(Resource csvResource, String mdn, int interval, int startIndex) {

        /* 2-1 CSV → List<CSV> (블로킹 I/O이므로 boundedElastic) */
        Mono<List<CSV>> listMono =
                Mono.fromCallable(() -> loadCsv(csvResource))
                        .subscribeOn(Schedulers.boundedElastic());

        return listMono
                .flatMapMany(all -> {
                    int from = Math.min(Math.max(startIndex, 0), all.size()); // 경계 보호
                    List<CSV> tail = all.subList(from, all.size());
                    return Flux.fromIterable(Lists.partition(tail, interval));
                })
                .delayElements(Duration.ofSeconds(interval))
                .flatMap(intervalChunk -> {
                    List<List<CSV>> packets = Lists.partition(intervalChunk, MAX_PACKET_SIZE);
                    return Flux.fromIterable(packets)
                            .concatMap(packet -> sendGpsBatch(mdn, packet));
                })
                .then();
    }

    /**
     * 관제 서버로 주기정보를 전송하는 메서드
     * @param mdn
     * @param batch
     * @return
     */
    private Mono<Void> sendGpsBatch(String mdn, List<CSV> batch) {

        Duration delta = timeShift.getOrDefault(mdn, Duration.ZERO);
        DateTimeFormatter OUT_MIN_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

        LocalDateTime firstShifted =
                LocalDateTime.parse(batch.get(0).getTimestamp(), CSV_FMT).plus(delta);
        String oTime = firstShifted.truncatedTo(ChronoUnit.MINUTES).format(OUT_MIN_FMT);

        AtomicInteger idx = new AtomicInteger(0);

        List<MdtGpsRequest.CList> cList = batch.stream()
                .map(csv -> {
                    int secVal = idx.getAndIncrement();     // 0,1,2 … 59

                    return MdtGpsRequest.CList.builder()
                            .sec(String.format("%02d", secVal))
                            .bat(csv.getBat())
                            .gps(Gps.builder()
                                    .gcd(csv.getGcd())
                                    .lat(csv.getLat())
                                    .lon(csv.getLon())
                                    .ang(csv.getAng())
                                    .spd(csv.getSpd())
                                    .sum(csv.getSum())
                                    .build())
                            .build();
                })
                .toList();

        MdtGpsRequest req = MdtGpsRequest.builder()
                .mdn(mdn)
                .mdt(MDT.decidedMDT())
                .oTime(oTime)
                .cCnt(String.valueOf(cList.size()))
                .cList(cList)
                .build();

        log.info("gps정보 확인 : {}",req);

        lastGps.put(mdn, batch.get(batch.size() - 1));

        return client.post()
                .uri("/gps")
                .header("Authorization", tokenService.getToken(mdn))
                .bodyValue(req)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(v ->
                        log.info("[{}] GPS 전송 성공 ({}건)", mdn, batch.size()))
                .doOnError(e ->
                        log.error("[{}] GPS 전송 실패: {}", mdn, e.getMessage()))
                .then();
    }
}
