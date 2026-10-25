package mx.com.fredkor.security.autoconfigure;

import mx.com.fredkor.security.authentication.*;
import mx.com.fredkor.security.bootstrap.RolesInitializer;
import mx.com.fredkor.security.filter.JwtAuthenticationFilter;
import mx.com.fredkor.security.handler.JwtAccessDeniedHandler;
import mx.com.fredkor.security.handler.JwtAuthenticationEntryPoint;
import mx.com.fredkor.security.jwt.*;
import mx.com.fredkor.security.persistence.entity.UsuarioEntity;
import mx.com.fredkor.security.persistence.repository.UsuarioRepository;
import mx.com.fredkor.security.persistence.repository.PermisoRepository;
import mx.com.fredkor.security.persistence.repository.RolRepository;
import mx.com.fredkor.security.properties.SecurityProperties;
import mx.com.fredkor.security.web.AuthenticationController;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import tools.jackson.databind.ObjectMapper;

@AutoConfiguration
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityProperties.class)
@EntityScan(basePackageClasses = UsuarioEntity.class)
@EnableJpaRepositories(basePackageClasses = UsuarioRepository.class)
@ConditionalOnClass({SecurityFilterChain.class, UsuarioRepository.class})
@ConditionalOnProperty(prefix = "fredkor.security", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SecurityAutoConfiguration {

    @Bean @ConditionalOnMissingBean
    RolesInitializer rolesInitializer(SecurityProperties properties, RolRepository roles,
            PermisoRepository permissions) {
        return new RolesInitializer(properties, roles, permissions);
    }

    @Bean @ConditionalOnMissingBean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean @ConditionalOnMissingBean
    UserDetailsService userDetailsService(UsuarioRepository repository) {
        return new UserDetailsServiceImpl(repository);
    }

    @Bean @ConditionalOnMissingBean
    JwtService jwtService(SecurityProperties properties) {
        return new JwtServiceImpl(properties.getJwt());
    }

    @Bean @ConditionalOnMissingBean
    JwtAuthenticationEntryPoint authenticationEntryPoint(ObjectMapper mapper) {
        return new JwtAuthenticationEntryPoint(mapper);
    }

    @Bean @ConditionalOnMissingBean
    JwtAccessDeniedHandler accessDeniedHandler(ObjectMapper mapper) {
        return new JwtAccessDeniedHandler(mapper);
    }

    @Bean @ConditionalOnMissingBean
    JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService,
            UserDetailsService userDetailsService, JwtAuthenticationEntryPoint entryPoint,
            SecurityProperties properties) {
        return new JwtAuthenticationFilter(jwtService, userDetailsService, entryPoint, properties);
    }

    @Bean @ConditionalOnMissingBean
    AuthenticationProvider authenticationProvider(UserDetailsService users, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(users);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean @ConditionalOnMissingBean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean @ConditionalOnMissingBean
    AuthenticationService authenticationService(UserDetailsService users,
            AuthenticationManager manager, JwtService jwtService) {
        return new AuthenticationServiceImpl(users, manager, jwtService);
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticationController.class)
    @ConditionalOnProperty(prefix = "fredkor.security", name = "login-endpoint-enabled",
            havingValue = "true", matchIfMissing = true)
    AuthenticationController authenticationController(AuthenticationService service) {
        return new AuthenticationController(service);
    }

    @Bean @ConditionalOnMissingBean
    CorsConfigurationSource corsConfigurationSource(SecurityProperties properties) {
        var configured = properties.getCors();
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowCredentials(configured.isAllowCredentials());
        cors.setAllowedOrigins(configured.getAllowedOrigins());
        cors.setAllowedMethods(configured.getAllowedMethods());
        cors.setAllowedHeaders(configured.getAllowedHeaders());
        cors.setExposedHeaders(configured.getExposedHeaders());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    @Bean @ConditionalOnMissingBean(SecurityFilterChain.class)
    SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityProperties properties,
            AuthenticationProvider provider, JwtAuthenticationFilter filter,
            JwtAuthenticationEntryPoint entryPoint, JwtAccessDeniedHandler deniedHandler,
            CorsConfigurationSource cors) throws Exception {
        String[] publicPaths = properties.getPublicPaths().toArray(String[]::new);
        http.csrf(csrf -> csrf.disable())
                .cors(configuration -> configuration.configurationSource(cors))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(errors -> errors.authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(deniedHandler))
                .authorizeHttpRequests(requests -> requests.requestMatchers(publicPaths).permitAll()
                        .anyRequest().authenticated())
                .authenticationProvider(provider)
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
