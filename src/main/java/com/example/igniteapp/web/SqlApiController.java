package com.example.igniteapp.web;

import com.example.igniteapp.api.dto.SqlExecuteRequest;
import com.example.igniteapp.api.dto.SqlExecuteResponse;
import com.example.igniteapp.service.SqlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
public class SqlApiController {

    private final SqlService sqlService;

    private static final Logger log = LoggerFactory.getLogger(SqlApiController.class);


    public SqlApiController(SqlService sqlService) {
        this.sqlService = sqlService;
    }

    @Operation(
            summary = "Выполнить SQL в Apache Ignite",
            description =
                    "Выполняет переданный SQL через Ignite thin JDBC.\n\n" +
                            "• Для SELECT вернёт: columns + rows.\n" +
                            "• Для DDL/DML вернёт: updateCount.\n\n" +
                            "Подсказка: лучше отправлять SQL без ';' на конце.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SqlExecuteRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Системная диагностика",
                                            value = "{ \"sql\": \"SELECT * FROM SYS.NODES\" }"
                                    ),
                                    @ExampleObject(
                                            name = "Создать таблицу",
                                            value = "{ \"sql\": \"CREATE TABLE test_table (id INT PRIMARY KEY, name VARCHAR) WITH \\\"CACHE_NAME=test_cache\\\"\" }"
                                    ),
                                    @ExampleObject(
                                            name = "Вставка и выборка",
                                            value = "{ \"sql\": \"INSERT INTO test_table (id, name) VALUES (1, 'hello')\" }"
                                    ),
                                    @ExampleObject(
                                            name = "SELECT из таблицы",
                                            value = "{ \"sql\": \"SELECT * FROM test_table\" }"
                                    )
                            }
                    )
            )
    )
    @ApiResponse(
            responseCode = "200",
            description = "Успех (ok=true) или ошибка (ok=false) в теле ответа",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = SqlExecuteResponse.class),
                    examples = {
                            @ExampleObject(
                                    name = "SELECT ответ",
                                    value = "{\n" +
                                            "  \"ok\": true,\n" +
                                            "  \"elapsedMs\": 7,\n" +
                                            "  \"columns\": [\"ONE\"],\n" +
                                            "  \"rows\": [{\"ONE\": 1}],\n" +
                                            "  \"updateCount\": null\n" +
                                            "}"
                            ),
                            @ExampleObject(
                                    name = "DML/DDL ответ",
                                    value = "{\n" +
                                            "  \"ok\": true,\n" +
                                            "  \"elapsedMs\": 9,\n" +
                                            "  \"columns\": [],\n" +
                                            "  \"rows\": [],\n" +
                                            "  \"updateCount\": 1\n" +
                                            "}"
                            ),
                            @ExampleObject(
                                    name = "Ошибка",
                                    value = "{\n" +
                                            "  \"ok\": false,\n" +
                                            "  \"elapsedMs\": 0,\n" +
                                            "  \"error\": \"Table \\\"NO_SUCH\\\" not found\",\n" +
                                            "  \"columns\": [],\n" +
                                            "  \"rows\": [],\n" +
                                            "  \"updateCount\": null\n" +
                                            "}"
                            )
                    }
            )
    )
    @PostMapping(
            value = "/sql",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SqlExecuteResponse exec(@org.springframework.web.bind.annotation.RequestBody SqlExecuteRequest req) {
        log.info("POST /api/sql from client");
        String sql = req != null ? req.getSql() : null;

        SqlExecuteResponse resp = new SqlExecuteResponse();

        try {
            if (sql == null) sql = "";
            // на всякий случай уберём один ';' в конце
            sql = sql.replaceAll(";\\s*$", "");

            Map<String, Object> raw = sqlService.execute(sql);

            resp.setOk(Boolean.TRUE.equals(raw.get("ok")));
            Object ms = raw.get("elapsedMs");
            resp.setElapsedMs(ms instanceof Number ? ((Number) ms).longValue() : 0L);

            resp.setError((String) raw.get("error"));

            @SuppressWarnings("unchecked")
            var cols = (java.util.List<String>) raw.getOrDefault("columns", Collections.emptyList());
            resp.setColumns(cols);

            @SuppressWarnings("unchecked")
            var rows = (java.util.List<Map<String, Object>>) raw.getOrDefault("rows", Collections.emptyList());
            resp.setRows(rows);

            Object uc = raw.get("updateCount");
            resp.setUpdateCount(uc instanceof Number ? ((Number) uc).intValue() : null);

            return resp;

        } catch (Exception e) {
            resp.setOk(false);
            resp.setElapsedMs(0L);
            resp.setError(e.getMessage());
            resp.setColumns(Collections.emptyList());
            resp.setRows(Collections.emptyList());
            resp.setUpdateCount(null);
            return resp;
        }
    }
}
