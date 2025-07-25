package com.example.emulator.util;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class CarRegistry {

    private final Map<String, Car> cars = new ConcurrentHashMap<>();

    /** 클래스패스의 txt를 읽어 250대 초기화 */
    @PostConstruct
    void init() throws IOException {

        try (var in = getClass().getClassLoader()
                .getResourceAsStream("CarNumber.txt");
             var br = new BufferedReader(
                     new InputStreamReader(in, StandardCharsets.UTF_8))) {

            br.lines()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(number -> cars.put(number, new Car(number)));

            log.info("Loaded {} car numbers", cars.size());

        } catch (Exception e) {
            throw new IllegalStateException("CarNumber.txt 로드 실패", e);
        }
    }

    public Collection<Car> all()              { return cars.values(); }
    public Car            get(String number)   { return cars.get(number); }
}
