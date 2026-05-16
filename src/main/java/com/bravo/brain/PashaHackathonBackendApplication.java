package com.bravo.brain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PashaHackathonBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(PashaHackathonBackendApplication.class, args);
    }
}
