package br.edu.atitus.camisa10.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Regras de acesso da API.
 *
 * Fluxo: o app envia "Authorization: Bearer <token>". O Spring Security
 * valida a assinatura e a expiração do token e transforma a claim "perfil"
 * em uma role (ROLE_VENDEDOR / ROLE_COMPRADOR). As regras abaixo decidem
 * quem pode acessar cada endpoint. Assim, a RF15 é garantida no backend:
 * um COMPRADOR que chama POST /api/produtos recebe 403.
 */
@Configuration
public class SecurityConfig {

    private static final String VENDEDOR = "VENDEDOR";
    private static final String COMPRADOR = "COMPRADOR";

    private final RespostasSeguranca respostasSeguranca;

    public SecurityConfig(RespostasSeguranca respostasSeguranca) {
        this.respostasSeguranca = respostasSeguranca;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // API sem sessão nem formulários: o token vai em cada requisição
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // permite que o console do H2 (perfil dev) seja exibido em frames
            .headers(h -> h.frameOptions(f -> f.sameOrigin()))

            .authorizeHttpRequests(auth -> auth
                // --- Públicos ---
                .requestMatchers(HttpMethod.POST, "/api/auth/registro", "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/categorias", "/api/produtos", "/api/produtos/*",
                        "/api/config/**", "/uploads/**", "/catalogo-fotos/**").permitAll()
                .requestMatchers("/h2-console/**", "/error").permitAll()

                // --- VENDEDOR (RF04 a RF08, RF15) ---
                .requestMatchers("/api/vendedor/**").hasRole(VENDEDOR)
                .requestMatchers(HttpMethod.POST, "/api/produtos", "/api/produtos/*/imagem",
                        "/api/produtos/*/imagem-catalogo").hasRole(VENDEDOR)
                .requestMatchers(HttpMethod.PUT, "/api/produtos/*").hasRole(VENDEDOR)
                .requestMatchers(HttpMethod.DELETE, "/api/produtos/*").hasRole(VENDEDOR)

                // --- COMPRADOR (RF13, RF14) ---
                .requestMatchers("/api/pedidos/**", "/api/pedidos").hasRole(COMPRADOR)

                // qualquer outra rota exige login
                .anyRequest().authenticated())

            // valida o JWT recebido no cabeçalho Authorization
            .oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt.jwtAuthenticationConverter(perfilComoRole()))
                .authenticationEntryPoint(respostasSeguranca)
                .accessDeniedHandler(respostasSeguranca))

            .exceptionHandling(e -> e
                .authenticationEntryPoint(respostasSeguranca)
                .accessDeniedHandler(respostasSeguranca));

        return http.build();
    }

    /** Lê a claim "perfil" do token e cria a role "ROLE_<perfil>". */
    private JwtAuthenticationConverter perfilComoRole() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName(TokenService.CLAIM_PERFIL);
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(roles);
        return conversor;
    }

    /** CORS liberado para desenvolvimento (Expo web, Postman, etc.). */
    private CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
