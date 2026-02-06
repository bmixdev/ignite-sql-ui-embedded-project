package com.example.igniteapp.service;

import com.example.igniteapp.config.IgniteProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

@Service
public class SqlService {

    private static final Logger log = LoggerFactory.getLogger(SqlService.class);

    private final IgniteProperties props;

    public SqlService(IgniteProperties props) {
        this.props = props;

        // на всякий случай регистрируем драйвер (не обязательно, но полезно)
        try {
            Class.forName("org.apache.ignite.IgniteJdbcThinDriver");
            log.info("Ignite JDBC Thin driver registered");
        } catch (ClassNotFoundException e) {
            // не критично, DriverManager может сам подхватить
            log.warn("Ignite JDBC Thin driver class not found: {}", e.getMessage());
        }
    }

    public Map<String, Object> execute(String sql) throws Exception {
        return executeWithLimit(sql, props.getMaxRows());
    }

    public Map<String, Object> executeWithLimit(String sql, int limitRows) throws Exception {
        long t0 = System.currentTimeMillis();

        Map<String, Object> result = new LinkedHashMap<>();
        String jdbcUrl = props.getJdbcUrl();

        String sqlPreview = safePreview(sql, 300);
        log.info("SQL execute start: limitRows={}, url={}, sql=\"{}\"",
                limitRows,
                safeJdbcUrl(jdbcUrl),
                sqlPreview
        );

        try (Connection c = DriverManager.getConnection(jdbcUrl);
             Statement st = c.createStatement()) {

            // таймаут на запрос (если поле есть в props)
            int timeout = props.getQueryTimeoutSeconds();
            if (timeout > 0) {
                st.setQueryTimeout(timeout);
            }

            // вот тут и есть limitRows
            if (limitRows > 0 && limitRows != props.getMaxRows()) {
                st.setMaxRows(limitRows);
            }

            boolean hasResultSet = st.execute(sql);

            long elapsed = System.currentTimeMillis() - t0;
            result.put("ok", true);
            result.put("elapsedMs", elapsed);

            if (!hasResultSet) {
                int upd = st.getUpdateCount();
                result.put("updateCount", upd);
                result.put("columns", Collections.emptyList());
                result.put("rows", Collections.emptyList());

                log.info("SQL execute done (non-select): updateCount={}, elapsedMs={}", upd, elapsed);
                return result;
            }

            try (ResultSet rs = st.getResultSet()) {
                ResultSetMetaData md = rs.getMetaData();

                List<String> columns = new ArrayList<>();
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    columns.add(md.getColumnLabel(i));
                }

                List<Map<String, Object>> rows = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= md.getColumnCount(); i++) {
                        row.put(columns.get(i - 1), rs.getObject(i));
                    }
                    rows.add(row);
                }

                result.put("columns", columns);
                result.put("rows", rows);
                result.put("rowCount", rows.size());
                result.put("limitRows", limitRows);

                log.info("SQL execute done (select): rowCount={}, colCount={}, elapsedMs={}",
                        rows.size(), columns.size(), elapsed);

                // для детального дебага можно включить DEBUG уровень
                if (log.isDebugEnabled()) {
                    log.debug("SQL result columns={}", columns);
                    log.debug("SQL result rowsPreview={}", rowsPreview(rows, 3));
                }

                return result;
            }
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - t0;

            // отдаём ответ в твоём формате, но НЕ глотаем исключение (пусть контроллер решает)
            log.error("SQL execute failed: elapsedMs={}, sql=\"{}\"", elapsed, sqlPreview, e);
            throw e;
        }
    }

    private static String safePreview(String sql, int maxLen) {
        if (sql == null) return "";
        String s = sql.replace('\n', ' ').replace('\r', ' ').trim();
        if (s.length() <= maxLen) return s;
        return s.substring(0, maxLen) + "...";
    }

    private static String safeJdbcUrl(String jdbcUrl) {
        // чтобы в лог не утекали возможные пароли/параметры
        if (jdbcUrl == null) return "";
        // простая маскировка типа ...password=xxx...
        return jdbcUrl.replaceAll("(?i)(password=)([^;]+)", "$1***");
    }

    private static List<Map<String, Object>> rowsPreview(List<Map<String, Object>> rows, int max) {
        if (rows == null || rows.isEmpty()) return Collections.emptyList();
        return rows.subList(0, Math.min(max, rows.size()));
    }
}
