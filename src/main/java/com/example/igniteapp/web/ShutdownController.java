package com.example.igniteapp.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ShutdownController {

    private final ApplicationContext context;

    public ShutdownController(ApplicationContext context) {
        this.context = context;
    }

    @Operation(
            summary = "Остановка приложения",
            description = "Корректно завершает работу Spring Boot приложения. " +
                    "HTTP-ответ возвращается до завершения процесса."
    )
    @ApiResponse(responseCode = "200", description = "Приложение уходит в shutdown")
    @PostMapping(value = "/shutdown", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> shutdown() {

        Map<String, Object> r = new LinkedHashMap<>();
        r.put("ok", true);
        r.put("status", "SHUTTING_DOWN");
        r.put("ts", Instant.now().toString());

        // Остановка в отдельном потоке, чтобы HTTP-ответ успел уйти
        new Thread(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException ignored) {
            }
            System.exit(SpringApplicationExit.exit(context));
        }, "shutdown-thread").start();

        return r;
    }
}
