package mx.com.fredkor.security.authentication;

import mx.com.fredkor.security.dto.LoginRequest;
import mx.com.fredkor.security.dto.LoginResponse;
import mx.com.fredkor.security.jwt.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;

public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationServiceImpl(UserDetailsService userDetailsService,
                                     AuthenticationManager authenticationManager,
                                     JwtService jwtService) {
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.username(), request.password()));
        return new LoginResponse(jwtService.generateToken(
                userDetailsService.loadUserByUsername(request.username())));
    }
}
