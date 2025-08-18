package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.example"})
public class EmulatorMain {
    public static void main(String[] args)  {
        SpringApplication.run(EmulatorMain.class, args);
    }
}