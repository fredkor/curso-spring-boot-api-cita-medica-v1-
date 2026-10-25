package mx.com.fredkor.apps.citamedica.shared.web.dtos;

import java.time.LocalDateTime;
import java.util.List;

public record ValidationErrorResponse(
        int status,
        String message,
        List<FieldValidationError> errors,
        LocalDateTime timestamp
) {
}