package com.example.igniteapp;

import com.example.igniteapp.config.IgniteProperties;
import com.example.igniteapp.security.AppSecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({IgniteProperties.class, AppSecurityProperties.class})
public class IgniteSqlUiApplication {
    public static void main(String[] args) {
        SpringApplication.run(IgniteSqlUiApplication.class, args);
    }
}
