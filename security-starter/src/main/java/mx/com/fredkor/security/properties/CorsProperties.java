package mx.com.fredkor.security.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CorsProperties {
    private boolean enabled = true;
    private boolean allowCredentials = true;
    private List<String> allowedOrigins = new ArrayList<>(List.of("http://localhost:4200"));
    private List<String> allowedMethods = new ArrayList<>(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    private List<String> allowedHeaders = new ArrayList<>(List.of("Authorization", "Content-Type", "Accept", "X-Trace-Id"));
    private List<String> exposedHeaders = new ArrayList<>(List.of("Authorization", "X-Trace-Id"));
}
