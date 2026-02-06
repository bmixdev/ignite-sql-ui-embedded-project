package com.example.igniteapp.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @Operation(
            summary = "Проверка живости приложения",
            description = "Быстрый endpoint для liveness-проб. Не проверяет Ignite, только то, что веб-приложение живо."
    )
    @ApiResponse(responseCode = "200", description = "ok=true если приложение живо")
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> health() {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("ok", true);
        r.put("status", "UP");
        r.put("ts", Instant.now().toString());
        return r;
    }
}
