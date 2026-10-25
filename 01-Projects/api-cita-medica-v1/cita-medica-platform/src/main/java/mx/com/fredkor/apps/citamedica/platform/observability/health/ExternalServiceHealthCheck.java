package mx.com.fredkor.apps.citamedica.platform.observability.health;

import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class ExternalServiceHealthCheck implements HealthIndicator {
    private final URI url;
    private final Duration timeout;
    private final HttpClient client;

    public ExternalServiceHealthCheck(
            @Value("${fredkor.observability.external-service.url}") String url,
            @Value("${fredkor.observability.external-service.timeout-ms:3000}") long timeoutMs) {
        this.url = URI.create(url);
        if (!this.url.isAbsolute() || !("https".equals(this.url.getScheme()) || "http".equals(this.url.getScheme()))) {
            throw new IllegalArgumentException("El health check requiere una URL HTTP o HTTPS absoluta.");
        }
        if (timeoutMs <= 0) throw new IllegalArgumentException("El timeout debe ser mayor que cero.");
        this.timeout = Duration.ofMillis(timeoutMs);
        this.client = HttpClient.newBuilder().connectTimeout(timeout)
                .followRedirects(HttpClient.Redirect.NEVER).build();
    }

    @Override
    public Health health() {
        var request = HttpRequest.newBuilder(url).timeout(timeout).GET().build();
        try {
            // Solo necesitamos el estado HTTP; no cargamos el HTML completo.
            var response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            try (var body = response.body()) {
                return (response.statusCode() == 200 ? Health.up() : Health.down())
                        .withDetail("url", url.toString())
                        .withDetail("expectedStatus", 200)
                        .withDetail("httpStatus", response.statusCode())
                        .build();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return unavailable("Consulta interrumpida.");
        } catch (IOException exception) {
            return unavailable("Sin respuesta HTTP dentro del tiempo permitido o error de conexión.");
        }
    }

    private Health unavailable(String reason) {
        return Health.down().withDetail("url", url.toString())
                .withDetail("expectedStatus", 200).withDetail("reason", reason).build();
    }

    @PreDestroy
    public void close() {
        client.close();
    }
}
