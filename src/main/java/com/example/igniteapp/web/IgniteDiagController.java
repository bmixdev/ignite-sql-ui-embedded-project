package com.example.igniteapp.web;

import com.example.igniteapp.api.dto.IgniteOverviewResponse;
import com.example.igniteapp.service.IgniteDiagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;

@RestController
@RequestMapping("/api/ignite")
public class IgniteDiagController {

    private final IgniteDiagService diagService;

    public IgniteDiagController(IgniteDiagService diagService) {
        this.diagService = diagService;
    }

    @Operation(
            summary = "Сводная диагностика Ignite",
            description =
                    "Возвращает агрегированный набор результатов по системным представлениям:\n" +
                            "• SYS.NODES\n" +
                            "• SYS.CACHES\n" +
                            "• SYS.SCHEMAS\n" +
                            "• SYS.SQL_QUERIES\n\n" +
                            "Формат каждого блока совпадает с /api/sql (columns + rows)."
    )
    @ApiResponse(responseCode = "200", description = "ok=true либо ok=false с error")
    @GetMapping("/overview")
    public IgniteOverviewResponse overview() {
        IgniteOverviewResponse resp = new IgniteOverviewResponse();
        try {
            resp.setOk(true);
            resp.setData(diagService.overview());
            return resp;
        } catch (Exception e) {
            resp.setOk(false);
            resp.setError(e.getMessage());
            resp.setData(new LinkedHashMap<>());
            return resp;
        }
    }
}
