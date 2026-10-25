package mx.com.fredkor.security.dto;

import java.time.LocalDateTime;

public record SecurityErrorResponse(
        String traceId,
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp
) {}
