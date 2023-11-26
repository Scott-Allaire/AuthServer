package org.coder229.authserver.model;

import java.util.List;

public record PageResponse<T>(Integer pageNum,
                              Integer pageSize,
                              Integer totalPages,
                              Long totalItems,
                              List<T> items) {
}
