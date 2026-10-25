package mx.com.fredkor.security.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "fredkor.security")
public class SecurityProperties {
    private boolean enabled = true;
    private boolean loginEndpointEnabled = true;
    private String loginBasePath = "/api/v1/auth";
    private List<String> publicPaths = new ArrayList<>(List.of(
            "/api/v1/auth/**", "/swagger-ui.html", "/swagger-ui/**",
            "/v3/api-docs/**", "/actuator/health"
    ));
    private Map<String, List<String>> roles = new LinkedHashMap<>();
    private JwtProperties jwt = new JwtProperties();
    private CorsProperties cors = new CorsProperties();
}
