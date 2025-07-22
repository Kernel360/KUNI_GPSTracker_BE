package com.example.car_emulator_server.service;

import com.example.car_emulator_server.util.Car;
import com.example.car_emulator_server.util.CarRegistry;
import com.example.car_emulator_server.util.CarState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmulatorService {

    private final CarRegistry carRegistry;

    public void changeState(String number, String state) {
        Car car = carRegistry.get(number);
        car.setState(CarState.valueOf(state));
    }

    public void sendOn(String number) {

    }

    public void sendOff(String number) {

    }
}
