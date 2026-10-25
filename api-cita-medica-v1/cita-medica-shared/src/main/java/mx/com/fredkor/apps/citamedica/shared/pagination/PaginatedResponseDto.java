package mx.com.fredkor.apps.citamedica.shared.pagination;

import java.util.List;

public record PaginatedResponseDto<T>(
        List<T> content,
        long totalElements,
        int page,
        int size
) {
}