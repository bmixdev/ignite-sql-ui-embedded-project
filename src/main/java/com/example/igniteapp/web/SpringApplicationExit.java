package com.example.igniteapp.web;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

public final class SpringApplicationExit {

    private SpringApplicationExit() {
    }

    public static int exit(ApplicationContext context) {
        return SpringApplication.exit(context, () -> 0);
    }
}
