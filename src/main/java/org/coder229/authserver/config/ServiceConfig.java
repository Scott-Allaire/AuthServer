package org.coder229.authserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "authservice.service")
public class ServiceConfig {
    private String jwtIssuer;
    private String jwtSecret;
    private String salt;
    private Duration jwtExpiration;
    private Duration refreshDuration;

    /** password expiration in months */
    private int passwordExpiration;

    public String getJwtIssuer() {
        return jwtIssuer;
    }

    public void setJwtIssuer(String jwtIssuer) {
        this.jwtIssuer = jwtIssuer;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Duration getJwtExpiration() {
        return jwtExpiration;
    }

    public void setJwtExpiration(Duration jwtExpiration) {
        this.jwtExpiration = jwtExpiration;
    }

    public Duration getRefreshDuration() {
        return refreshDuration;
    }

    public void setRefreshDuration(Duration refreshDuration) {
        this.refreshDuration = refreshDuration;
    }

    public int getPasswordExpiration() {
        return passwordExpiration;
    }

    public void setPasswordExpiration(int passwordExpiration) {
        this.passwordExpiration = passwordExpiration;
    }
}
