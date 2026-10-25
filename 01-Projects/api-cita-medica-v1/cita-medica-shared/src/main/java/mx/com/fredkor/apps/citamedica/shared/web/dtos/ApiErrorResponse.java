package mx.com.fredkor.apps.citamedica.shared.web.dtos;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(
        String traceId,
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp,
        List<FieldValidationError> fieldErrors
) {
}
