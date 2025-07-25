package com.example.emulator.util;

import lombok.*;

@Getter
@Setter
@Builder
public class Car {
    private String number;// 예: 28나3920
    @Builder.Default
    private CarState state = CarState.OFF;

    public Car(String number, CarState state) {
        this.number = number;
        this.state = state;
    }


    //private ScheduledFuture<?> task;    // GPS 생성 스케줄 핸들
}
