package org.coder229.authserver.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RefreshRequest(
        @NotNull
        Long userId,
        @NotBlank
        String refreshToken) {
}
