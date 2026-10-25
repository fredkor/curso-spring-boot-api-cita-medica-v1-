package mx.com.fredkor.apps.citamedica.platform.configuration;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import mx.com.fredkor.apps.citamedica.shared.web.dtos.ApiErrorResponse;
import mx.com.fredkor.security.dto.SecurityErrorResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Map;
import java.util.List;

@Configuration
public class ApiErrorDocumentation {
    @Bean
    public OpenApiCustomizer errorResponses() {
        return api -> {
            if (api.getComponents() == null) api.setComponents(new io.swagger.v3.oas.models.Components());
            ModelConverters.getInstance().readAll(ApiErrorResponse.class).forEach(api.getComponents()::addSchemas);
            ModelConverters.getInstance().readAll(SecurityErrorResponse.class).forEach(api.getComponents()::addSchemas);
            if (api.getPaths() == null) return;
            api.getPaths().forEach((path, item) -> item.readOperationsMap().forEach((method, operation) -> {
                if (operation.getResponses() == null) operation.setResponses(new ApiResponses());
                var responses = operation.getResponses();
                responses.addApiResponse("400", response(400, "VALIDATION_ERROR", "Datos o parámetros inválidos.", path, false));
                responses.addApiResponse("401", response(401, "UNAUTHORIZED", "Autenticación requerida o credenciales inválidas.", path, true));
                responses.addApiResponse("403", response(403, "FORBIDDEN", "Permisos insuficientes.", path, true));
                responses.addApiResponse("500", response(500, "INTERNAL_SERVER_ERROR", "Ocurrió un error interno.", path, false));
                if (path.contains("{id}") || method.name().equals("POST") || method.name().equals("PUT") || method.name().equals("PATCH")) {
                    responses.addApiResponse("404", response(404, "NOT_FOUND", "Recurso o referencia no encontrado.", path, false));
                }
                if (List.of("POST", "PUT", "PATCH", "DELETE").contains(method.name())) {
                    responses.addApiResponse("409", response(409, "DATA_INTEGRITY_ERROR", "Conflicto con los datos existentes.", path, false));
                }
                if (List.of("POST", "PUT").contains(method.name())) {
                    responses.addApiResponse("415", response(415, "UNSUPPORTED_MEDIA_TYPE", "Tipo de contenido no admitido.", path, false));
                }
                if (method.name().equals("DELETE") || (method.name().equals("GET") && !path.contains("{"))) {
                    responses.addApiResponse("204", new ApiResponse().description("Sin contenido."));
                }
                if (path.endsWith("/auth/login")) operation.setSecurity(List.of());
            }));
        };
    }

    private ApiResponse response(int status, String code, String message, String path, boolean security) {
        Schema<?> schema = new Schema<>().$ref("#/components/schemas/ApiErrorResponse");
        if (security) {
            schema = new ComposedSchema().addAnyOfItem(schema)
                .addAnyOfItem(new Schema<>().$ref("#/components/schemas/SecurityErrorResponse"));
        }
        var example = new java.util.LinkedHashMap<String, Object>();
        example.put("traceId", "ejemplo-trace-id");
        example.put("status", status);
        example.put("error", code);
        example.put("message", message);
        example.put("path", path);
        example.put("timestamp", "2027-01-20T09:00:00");
        if (!security) example.put("fieldErrors", status == 400
            ? List.of(Map.of("field", "nombre", "message", "no debe estar vacío")) : List.of());
        return new ApiResponse().description(message).content(new Content().addMediaType(
            "application/json", new MediaType().schema(schema).example(example)));
    }
}
