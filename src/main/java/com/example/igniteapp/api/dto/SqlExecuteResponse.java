package com.example.igniteapp.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

@Schema(name = "SqlExecuteResponse", description = "Ответ выполнения SQL.")
public class SqlExecuteResponse {

    @Schema(description = "Успех выполнения", example = "true")
    private boolean ok;

    @Schema(description = "Время выполнения на стороне приложения (мс)", example = "12")
    private long elapsedMs;

    @Schema(description = "Текст ошибки (если ok=false)", example = "Failed to connect ...")
    private String error;

    @Schema(description = "Имена колонок результата (для SELECT)")
    private List<String> columns;

    @Schema(description = "Строки результата (для SELECT). Каждая строка = map {column -> value}")
    private List<Map<String, Object>> rows;

    @Schema(description = "Количество затронутых строк (для non-SELECT)", example = "1")
    private Integer updateCount;

    public boolean isOk() { return ok; }
    public void setOk(boolean ok) { this.ok = ok; }

    public long getElapsedMs() { return elapsedMs; }
    public void setElapsedMs(long elapsedMs) { this.elapsedMs = elapsedMs; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public List<String> getColumns() { return columns; }
    public void setColumns(List<String> columns) { this.columns = columns; }

    public List<Map<String, Object>> getRows() { return rows; }
    public void setRows(List<Map<String, Object>> rows) { this.rows = rows; }

    public Integer getUpdateCount() { return updateCount; }
    public void setUpdateCount(Integer updateCount) { this.updateCount = updateCount; }
}
