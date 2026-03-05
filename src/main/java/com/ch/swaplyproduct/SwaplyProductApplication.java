package com.ch.swaplyproduct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SwaplyProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwaplyProductApplication.class, args);
    }

}
