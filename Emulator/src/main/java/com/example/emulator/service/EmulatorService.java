package com.example.emulator.service;


import com.example.emulator.model.*;
import com.example.emulator.util.Car;
import com.example.emulator.util.CarRegistry;
import com.example.emulator.util.CarState;
import com.google.common.collect.Lists;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmulatorService {

    private final WebClient client;
    private final CarRegistry carRegistry;
    private final TokenService tokenService;

    //전송 스트림 관리를 위한 Map
    private final ConcurrentMap<String, Disposable> running = new ConcurrentHashMap<>();
    //처음과 마지막 전송 데이터 저장
    private final ConcurrentMap<String, CSV> firstGps = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, CSV> lastGps = new ConcurrentHashMap<>();

    public void changeState(String number, String state) {
        Car car = carRegistry.get(number);
        car.setState(CarState.valueOf(state));
    }
    public void start(Path csv, String mdn) throws IOException {
        stop(mdn);   // 같은 차량이 이미 돌고 있으면 중단

        Disposable d = sendOnAndStart(csv, mdn)         //  /on 전송 →  60초 주기 /gps
                .doOnTerminate(() -> stop(mdn))
                .subscribe(
                        null,
                        err -> log.error("[{}] 스트림 오류: {}", mdn, err.toString())
                );

        running.put(mdn, d);
        changeState(mdn, "ON");
    }

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

    private Mono<Void> sendOff(String mdn) {

        CSV last = lastGps.get(mdn);
        CSV first = firstGps.get(mdn);

        if(last == null) last = first;

        if (last == null || first == null) {
            log.warn("[{}] first/last GPS 없음 → CarStop 생략", mdn);
            return Mono.empty();
        }

        DateTimeFormatter inFmt  = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter outFmt = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

        String offTime = LocalDateTime.parse(last.getTimestamp(), inFmt)
                .format(outFmt);
        String onTime = LocalDateTime.parse(first.getTimestamp(), inFmt)
                .format(outFmt);

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
                .header("Token", tokenService.getToken(mdn))
                .bodyValue(req)     // 규격에 맞춰 수정
                .retrieve()
                .toBodilessEntity()
                .then();
    }

    /**
     * /on 전송 → 60초마다 /gps 전송
     * @param csvPath CSV 파일
     * @param mdn     차량 번호
     */
    public Mono<Void> sendOnAndStart(Path csvPath, String mdn) throws IOException {

        return sendOn(csvPath, mdn)                 // 1. ON
                .then(startGps(csvPath, mdn));  // 2. 주기 전송
    }


    public Mono<Void> sendOn(Path csvPath, String number) throws IOException {

        List<CSV> all = loadCsv(csvPath);

        CSV csvFirst = all.get(0);

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
                .onTime(csvFirst.getTimestamp())
                .offTime(null)
                .gps(gps)
                .build();

        return client.post()
                .uri("/on")
                .header("Token", tokenService.getToken(number))
                .bodyValue(req)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(v -> log.info("[{}] ON 전송 성공", number))
                .doOnError(e -> log.error("[{}] ON 전송 실패: {}", number, e.getMessage()))
                .then();
    }


    public List<CSV> loadCsv(Path csvPath) throws IOException {
        try (Reader reader = Files.newBufferedReader(csvPath)) {

            var csvToBean = new CsvToBeanBuilder<CSV>(reader)
                    .withType(CSV.class)
                    .withIgnoreLeadingWhiteSpace(true) // 앞뒤 공백 무시
                    .withSeparator(',')
                    .withSkipLines(0)
                    .build();

            return csvToBean.parse();
        }
    }

    /** CSV를 한 번 읽고 60초 간격으로 전송 */
    private Mono<Void> startGps(Path csv, String mdn) {

        /* 2-1 CSV → List<CSV> (블로킹 I/O이므로 boundedElastic) */
        Mono<List<CSV>> listMono =
                Mono.fromCallable(() -> loadCsv(csv))
                        .subscribeOn(Schedulers.boundedElastic());

        /* 2-2 60개씩 끊어 60초마다 POST */
        return listMono.flatMapMany(all -> {
                    List<List<CSV>> batches = Lists.partition(all, 60);
                    return Flux.fromIterable(batches);
                })
                .delayElements(Duration.ofSeconds(60))
                .flatMap(batch -> sendGpsBatch(mdn, batch))
                .then();                   // Mono<Void>
    }

    private Mono<Void> sendGpsBatch(String mdn, List<CSV> batch) {

        List<MdtGpsRequest.CList> cList = batch.stream()
                .map(csv -> MdtGpsRequest.CList.builder()
                        .sec(csv.getSec())          // 발생시간 '초'
                        .bat(csv.getBat())          // 배터리 전압
                        .gps(Gps.builder()          // GPS 세부 정보
                                .gcd(csv.getGcd())
                                .lat(csv.getLat())
                                .lon(csv.getLon())
                                .ang(csv.getAng())
                                .spd(csv.getSpd())
                                .sum(csv.getSum())
                                .build())
                        .build())
                .toList();

        DateTimeFormatter inFmt  = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter outFmt = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

        String oTime = LocalDateTime.parse(batch.get(0).getTimestamp(), inFmt)
                .format(outFmt);


        MdtGpsRequest req = MdtGpsRequest.builder()
                .mdn(mdn)
                .mdt(MDT.decidedMDT())
                .oTime(oTime)
                .cCnt(String.valueOf(cList.size()))
                .cList(cList)
                .build();

        lastGps.put(mdn, batch.get(batch.size() - 1));

        return client.post()
                .uri("/gps")
                .header("Token", tokenService.getToken(mdn))
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
