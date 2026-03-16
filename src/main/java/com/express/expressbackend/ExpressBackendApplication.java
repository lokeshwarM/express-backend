package com.express.expressbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = "com.express.expressbackend.domain")
public class ExpressBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExpressBackendApplication.class, args);
    }
}


