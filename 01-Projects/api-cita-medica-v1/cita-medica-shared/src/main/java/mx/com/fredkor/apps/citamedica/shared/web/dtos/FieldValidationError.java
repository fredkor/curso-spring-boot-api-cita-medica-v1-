package mx.com.fredkor.apps.citamedica.shared.web.dtos;

public record FieldValidationError(
        String field,
        String message
) {
}