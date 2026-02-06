package com.example.igniteapp.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SqlExecuteRequest", description = "Запрос на выполнение SQL в Apache Ignite (thin JDBC).")
public class SqlExecuteRequest {

    @Schema(
            description = "SQL строка. Лучше без ';' на конце.",
            example = "SELECT * FROM SYS.NODES",
            required = true
    )
    private String sql;

    public SqlExecuteRequest() {}

    public SqlExecuteRequest(String sql) {
        this.sql = sql;
    }

    public String getSql() { return sql; }
    public void setSql(String sql) { this.sql = sql; }
}
