package com.example.reportgenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.example.reportgenerator.repository")
public class ReportGeneratorApp {

    public static void main(String[] args) {
        SpringApplication.run(ReportGeneratorApp.class, args);
    }

}
