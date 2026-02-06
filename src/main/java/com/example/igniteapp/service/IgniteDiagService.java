package com.example.igniteapp.service;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class IgniteDiagService {

    private final SqlService sqlService;

    public IgniteDiagService(SqlService sqlService) {
        this.sqlService = sqlService;
    }

    public Map<String, Object> overview() throws Exception {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("nodes", sqlService.execute("SELECT * FROM SYS.NODES"));
        res.put("caches", sqlService.execute("SELECT * FROM SYS.CACHES"));
        res.put("schemas", sqlService.execute("SELECT * FROM SYS.SCHEMAS"));
        res.put("activeQueries", sqlService.execute("SELECT * FROM SYS.SQL_QUERIES"));
        return res;
    }
}
