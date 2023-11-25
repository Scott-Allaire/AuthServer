package org.coder229.authserver.model;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record RegisterRequest(
        @NotBlank
        @Length(min = 4, max = 35)
        String username,

        @NotBlank
        @Length(min = 8, max = 200)
        String password) {
}
