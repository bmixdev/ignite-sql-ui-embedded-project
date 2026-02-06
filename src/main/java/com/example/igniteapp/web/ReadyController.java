package com.example.igniteapp.web;

import com.example.igniteapp.config.IgniteProperties;
import com.example.igniteapp.service.SqlService;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReadyController {

    private final SqlService sqlService;
    private final IgniteProperties igniteProps;

    public ReadyController(SqlService sqlService, IgniteProperties igniteProps) {
        this.sqlService = sqlService;
        this.igniteProps = igniteProps;
    }

    @GetMapping("/ready")
    public Map<String, Object> ready() throws Exception {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("ts", Instant.now().toString());

        r.put("mode", igniteProps.getEmbedded().isEnabled() ? "embedded" : "external");

        // --- SELECT 1 ---
        long t0 = System.nanoTime();
        Map<String, Object> q1 = sqlService.executeWithLimit("SELECT 1", 1);
        q1.put("elapsedMs", (System.nanoTime() - t0) / 1_000_000);
        r.put("select1", q1);

        // --- SYS.NODES ---
        long t1 = System.nanoTime();
        Map<String, Object> nodes = sqlService.executeWithLimit(
                "SELECT CONSISTENT_ID FROM SYS.NODES",
                10
        );
        nodes.put("elapsedMs", (System.nanoTime() - t1) / 1_000_000);

        if (Boolean.TRUE.equals(nodes.get("ok"))) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows =
                    (List<Map<String, Object>>) nodes.get("rows");
            nodes.put("nodeCount", rows.size());
        }

        r.put("nodes", nodes);

        boolean ready =
                Boolean.TRUE.equals(q1.get("ok")) &&
                        Boolean.TRUE.equals(nodes.get("ok"));

        r.put("ok", ready);
        r.put("status", ready ? "READY" : "NOT_READY");

        return r;
    }
}
