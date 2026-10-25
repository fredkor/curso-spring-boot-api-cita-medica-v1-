package mx.com.fredkor.security.authentication;

import mx.com.fredkor.security.dto.LoginRequest;
import mx.com.fredkor.security.dto.LoginResponse;

public interface AuthenticationService {
    LoginResponse login(LoginRequest request);
}
