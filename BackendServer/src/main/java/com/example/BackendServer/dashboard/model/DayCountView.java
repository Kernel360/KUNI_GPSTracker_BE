package com.example.BackendServer.dashboard.model;

import java.time.LocalDate;

public interface DayCountView {
    LocalDate getDay();
    long getTotalCar();
}
