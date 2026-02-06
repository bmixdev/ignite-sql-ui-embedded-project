package com.example.igniteapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ignite")
public class IgniteProperties {

    private String jdbcUrl = "jdbc:ignite:thin://127.0.0.1:10800";
    private int queryTimeoutSeconds = 10;
    private int maxRows = 500;

    private Embedded embedded = new Embedded();

    public String getJdbcUrl() { return jdbcUrl; }
    public void setJdbcUrl(String jdbcUrl) { this.jdbcUrl = jdbcUrl; }

    public int getQueryTimeoutSeconds() { return queryTimeoutSeconds; }
    public void setQueryTimeoutSeconds(int queryTimeoutSeconds) { this.queryTimeoutSeconds = queryTimeoutSeconds; }

    public int getMaxRows() { return maxRows; }
    public void setMaxRows(int maxRows) { this.maxRows = maxRows; }

    public Embedded getEmbedded() { return embedded; }
    public void setEmbedded(Embedded embedded) { this.embedded = embedded; }

    public static class Embedded {
        private boolean enabled = false;
        private String instanceName = "ignite-embedded";
        private String workDir = "ignite-work"; // лучше задавать абсолютный в yml
        private int thinPort = 10800;
        private boolean persistenceEnabled = false;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public String getInstanceName() { return instanceName; }
        public void setInstanceName(String instanceName) { this.instanceName = instanceName; }

        public String getWorkDir() { return workDir; }
        public void setWorkDir(String workDir) { this.workDir = workDir; }

        public int getThinPort() { return thinPort; }
        public void setThinPort(int thinPort) { this.thinPort = thinPort; }

        public boolean isPersistenceEnabled() { return persistenceEnabled; }
        public void setPersistenceEnabled(boolean persistenceEnabled) { this.persistenceEnabled = persistenceEnabled; }
    }
}
