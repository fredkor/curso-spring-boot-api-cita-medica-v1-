package mx.com.fredkor.security.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JwtProperties {
    private String signingKey;
    private String issuer = "FREDKOR";
    private Duration expiration = Duration.ofMinutes(10);
}
