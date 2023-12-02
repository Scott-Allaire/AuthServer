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
    private Duration jwtDuration;
    private Duration refreshDuration;

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

    public Duration getJwtDuration() {
        return jwtDuration;
    }

    public void setJwtDuration(Duration jwtDuration) {
        this.jwtDuration = jwtDuration;
    }

    public Duration getRefreshDuration() {
        return refreshDuration;
    }

    public void setRefreshDuration(Duration refreshDuration) {
        this.refreshDuration = refreshDuration;
    }
}
