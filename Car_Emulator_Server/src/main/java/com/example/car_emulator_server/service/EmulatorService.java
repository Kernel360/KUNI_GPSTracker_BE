package com.example.car_emulator_server.service;

import com.example.car_emulator_server.model.*;
import com.example.car_emulator_server.util.Car;
import com.example.car_emulator_server.util.CarRegistry;
import com.example.car_emulator_server.util.CarState;
import com.google.common.collect.Lists;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
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
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmulatorService {

    private final WebClient client;
    private final CarRegistry carRegistry;
    private final TokenService tokenService;

    public void changeState(String number, String state) {
        Car car = carRegistry.get(number);
        car.setState(CarState.valueOf(state));
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

        tokenService.getToken(number);

        List<CSV> all = loadCsv(csvPath);

        CSV csvFirst = all.get(0);

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
                .onTime(String.valueOf(LocalDateTime.now()))
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

    public void sendOff(String number) {

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
                .flatMap(batch -> postGpsBatch(mdn, batch))
                .then();                   // Mono<Void>
    }

    private Mono<Void> postGpsBatch(String mdn, List<CSV> batch) {

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

        MdtGpsRequest req = MdtGpsRequest.builder()
                .mdn(mdn)
                .mdt(MDT.decidedMDT())
                .oTime(LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")))
                .cCnt(String.valueOf(cList.size()))
                .cList(cList)
                .build();

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
