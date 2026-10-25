package mx.com.fredkor.apps.citamedica.api;

import com.sun.net.httpserver.HttpServer;
import mx.com.fredkor.apps.citamedica.platform.observability.health.ExternalServiceHealthCheck;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.health.contributor.Status;
import java.net.InetSocketAddress;
import static org.junit.jupiter.api.Assertions.*;

class ExternalServiceHealthCheckTest {
    @ParameterizedTest
    @ValueSource(ints = {200, 204, 302, 404, 503})
    void onlyHttp200IsHealthy(int code) throws Exception {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/auth/login", exchange -> {
            assertEquals("GET", exchange.getRequestMethod());
            if (code == 302) exchange.getResponseHeaders().set("Location", "/healthy");
            exchange.sendResponseHeaders(code, -1); exchange.close();
        });
        server.createContext("/healthy", exchange -> { exchange.sendResponseHeaders(200, -1); exchange.close(); });
        server.start();
        var check = new ExternalServiceHealthCheck("http://127.0.0.1:" + server.getAddress().getPort() + "/auth/login", 1000);
        try {
            var health = check.health();
            assertEquals(code == 200 ? Status.UP : Status.DOWN, health.getStatus());
            assertEquals(code, health.getDetails().get("httpStatus"));
        } finally { check.close(); server.stop(0); }
    }

    @Test
    void slowResponseAndUnreachableServerAreDown() throws Exception {
        var server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/slow", exchange -> {
            try { Thread.sleep(500); exchange.sendResponseHeaders(200, -1); }
            catch (InterruptedException exception) { Thread.currentThread().interrupt(); }
            finally { exchange.close(); }
        });
        server.start();
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/slow";
        var check = new ExternalServiceHealthCheck(url, 100);
        try { assertEquals(Status.DOWN, check.health().getStatus()); }
        finally { check.close(); server.stop(0); }
        var unavailable = new ExternalServiceHealthCheck(url, 100);
        try { assertEquals(Status.DOWN, unavailable.health().getStatus()); }
        finally { unavailable.close(); }
    }
}
