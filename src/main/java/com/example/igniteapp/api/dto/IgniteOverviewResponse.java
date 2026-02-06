package com.example.igniteapp.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(name = "IgniteOverviewResponse", description = "Диагностическая сводка Ignite: узлы/кэши/схемы/активные запросы.")
public class IgniteOverviewResponse {

    @Schema(description = "Успех", example = "true")
    private boolean ok;

    @Schema(description = "Ошибка (если ok=false)")
    private String error;

    @Schema(description = "Данные. Внутри ключи: nodes, caches, schemas, activeQueries (каждый как результат /api/sql).")
    private Map<String, Object> data;

    public boolean isOk() { return ok; }
    public void setOk(boolean ok) { this.ok = ok; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
}
