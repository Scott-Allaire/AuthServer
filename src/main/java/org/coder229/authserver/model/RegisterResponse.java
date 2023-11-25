package org.coder229.authserver.model;

public record RegisterResponse(String username, Boolean enabled, Boolean verified) {
}
