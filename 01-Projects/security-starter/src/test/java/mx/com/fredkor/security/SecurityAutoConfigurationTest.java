package mx.com.fredkor.security;

import mx.com.fredkor.security.jwt.JwtServiceImpl;
import mx.com.fredkor.security.properties.JwtProperties;
import mx.com.fredkor.security.properties.SecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.security.core.userdetails.User;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityAutoConfigurationTest {
    private static final String TEST_KEY =
            "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    @Test
    void exposesSafeConfigurationDefaults() {
        SecurityProperties properties = new SecurityProperties();
        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getPublicPaths()).contains("/actuator/health");
        assertThat(properties.getJwt().getExpiration()).isEqualTo(Duration.ofMinutes(10));
    }

    @Test
    void bindsConfiguredRolesAndPermissions() {
        var source = new MapConfigurationPropertySource(Map.of(
                "fredkor.security.roles.administrador[0]", "USUARIO_CREAR",
                "fredkor.security.roles.administrador[1]", "USUARIO_VER"
        ));

        SecurityProperties properties = new Binder(source)
                .bind("fredkor.security", Bindable.of(SecurityProperties.class))
                .orElseThrow(() -> new AssertionError("Security properties were not bound"));

        assertThat(properties.getRoles()).containsEntry(
                "administrador", java.util.List.of("USUARIO_CREAR", "USUARIO_VER"));
    }

    @Test
    void generatesAndValidatesToken() {
        JwtProperties properties = new JwtProperties();
        properties.setSigningKey(TEST_KEY);
        properties.setIssuer("starter-test");
        JwtServiceImpl service = new JwtServiceImpl(properties);
        var user = User.withUsername("fredkor").password("irrelevant")
                .authorities("CITA_VER").build();

        String token = service.generateToken(user);

        assertThat(service.extractUser(token)).isEqualTo("fredkor");
        assertThat(service.isTokenValid(token, user)).isTrue();
    }
}
