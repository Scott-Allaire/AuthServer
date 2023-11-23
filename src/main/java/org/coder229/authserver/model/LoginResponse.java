package org.coder229.authserver.model;

import java.time.Instant;

public record LoginResponse(String accessToken,
                            String refreshToken,
                            Instant expires) {
}
