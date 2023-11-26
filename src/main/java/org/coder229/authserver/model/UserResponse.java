package org.coder229.authserver.model;

import java.util.List;

public record UserResponse(Long id, String username, Boolean enabled, Boolean verified, List<String> roles) {
}
