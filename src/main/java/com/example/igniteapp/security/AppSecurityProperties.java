package com.example.igniteapp.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {

    /**
     * none | keycloak | fakelogin
     */
    private String mode = "none";

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public boolean isNone() { return "none".equalsIgnoreCase(mode); }
    public boolean isKeycloak() { return "keycloak".equalsIgnoreCase(mode); }
    public boolean isFakelogin() { return "fakelogin".equalsIgnoreCase(mode); }
}
