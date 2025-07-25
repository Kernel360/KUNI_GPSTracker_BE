package com.example.emulator.service;


import com.example.emulator.model.*;
import com.example.emulator.util.Car;
import com.example.emulator.util.CarRegistry;
import com.example.emulator.util.CarState;
import com.google.common.collect.Lists;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
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
import java.util.ArrayList;
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

    /**
     * 차량의 상태를 on/off 로 변경한다.
     * @param number
     * @param state
     */
    public void changeState(String number, String state) {
        Car car = carRegistry.get(number);
        car.setState(CarState.valueOf(state));
    }

    /**
     * 차량에 대응되는 csv파일을 읽어서 interval 초 단위로 관제 서버로 데이터를 보내는 메서드
     * sendOnAndStart 메서드를 사용해서 ON 요청과 주기정보 요청을 보내는 메서드를 호출 한다.
     *
     * running에 현재 요청 스트림을 유지한다.
     *
     * @param csv
     * @param mdn
     * @param interval
     * @throws IOException
     */
    public void start(Path csv, String mdn, int interval) throws IOException {
        stop(mdn);   // 같은 차량이 이미 돌고 있으면 중단

        Disposable d = sendOnAndStart(csv, mdn, interval)         //  /on 전송 →  interval 주기 /gps
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

        CSV last = lastGps.get(mdn);
        CSV first = firstGps.get(mdn);

        if(last == null) last = first;

        if (last == null || first == null) {
            log.warn("[{}] first/last GPS 없음 → CarStop 생략", mdn);
            return Mono.empty();
        }

        DateTimeFormatter inFmt  = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter outFmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

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
                .header("Authorization", tokenService.getToken(mdn))
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
    public Mono<Void> sendOnAndStart(Path csvPath, String mdn, int interval) throws IOException {

        return sendOn(csvPath, mdn)                 // 1. ON
                .then(startGps(csvPath, mdn, interval));  // 2. 주기 전송
    }

    /**
     * 관제 서버로 On 요청을 보내느 메서드
     * @param csvPath
     * @param number
     * @return
     * @throws IOException
     */
    public Mono<Void> sendOn(Path csvPath, String number) throws IOException {

        List<CSV> all = loadCsv(csvPath);

        CSV csvFirst = all.get(0);

        DateTimeFormatter inFmt  = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter outFmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        String onTime = LocalDateTime.parse(csvFirst.getTimestamp(), inFmt)
                .format(outFmt);

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
     * @param csvPath
     * @return
     * @throws IOException
     */
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

    /** CSV를 한 번 읽고 interval초 간격으로 전송 */
    private Mono<Void> startGps(Path csv, String mdn, int interval) {

        /* 2-1 CSV → List<CSV> (블로킹 I/O이므로 boundedElastic) */
        Mono<List<CSV>> listMono =
                Mono.fromCallable(() -> loadCsv(csv))
                        .subscribeOn(Schedulers.boundedElastic());

        /* 2-2 60개씩 끊어 60초마다 POST */
        return listMono.flatMapMany(all -> {
                    List<List<CSV>> batches = Lists.partition(all, interval);
                    return Flux.fromIterable(batches);
                })
                .delayElements(Duration.ofSeconds(interval))
                .flatMap(batch -> sendGpsBatch(mdn, batch))
                .then();                   // Mono<Void>
    }

    /**
     * 관제 서버로 주기정보를 전송하는 메서드
     * @param mdn
     * @param batch
     * @return
     */
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


    /**
     * 아래 4가지 메서드는
     * 애뮬레이터 웹에서 전체 차량 ON/Off , 범위 지정 ON/Off 기능을 구현한 메서드 들이다.
     *
     */


    public void turnOnAll(int interval) {
        for (Car car : carRegistry.all()) {
            try {
                Path csvPath = new ClassPathResource("gps/" + car.getNumber() + ".csv").getFile().toPath();
                start(csvPath, car.getNumber(), interval);
            } catch (Exception e) {
                log.error("[{}] 전체 ON 처리 중 오류 발생: {}", car.getNumber(), e.getMessage());
            }
        }
    }

    public void turnOffAll() {
        for (Car car : carRegistry.all()) {
            try {
                stop(car.getNumber());
            } catch (Exception e) {
                log.error("[{}] 전체 OFF 처리 중 오류 발생: {}", car.getNumber(), e.getMessage());
            }
        }
    }

    public void turnOnRange(int startIndex, int endIndex, int interval) {
        List<Car> cars = new ArrayList<>(carRegistry.all());
        for (int i = startIndex - 1; i < endIndex && i < cars.size(); i++) {
            Car car = cars.get(i);
            try {
                Path csvPath = new ClassPathResource("gps/" + car.getNumber() + ".csv").getFile().toPath();
                start(csvPath, car.getNumber(), interval);
            } catch (Exception e) {
                log.error("[{}] 범위 ON 처리 중 오류 발생: {}", car.getNumber(), e.getMessage());
            }
        }
    }

    public void turnOffRange(int startIndex, int endIndex) {
        List<Car> cars = new ArrayList<>(carRegistry.all());
        for (int i = startIndex - 1; i < endIndex && i < cars.size(); i++) {
            Car car = cars.get(i);
            try {
                stop(car.getNumber());
            } catch (Exception e) {
                log.error("[{}] 범위 OFF 처리 중 오류 발생: {}", car.getNumber(), e.getMessage());
            }
        }
    }
}
