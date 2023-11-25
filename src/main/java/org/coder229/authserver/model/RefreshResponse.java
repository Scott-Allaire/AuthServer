package org.coder229.authserver.model;

import java.time.Instant;

public record RefreshResponse(String accessToken,
                              Instant expires) {
}
