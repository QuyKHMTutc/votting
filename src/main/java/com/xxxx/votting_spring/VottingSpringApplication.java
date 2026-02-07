package com.xxxx.votting_spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
@org.springframework.scheduling.annotation.EnableAsync
public class VottingSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(VottingSpringApplication.class, args);
    }

}
