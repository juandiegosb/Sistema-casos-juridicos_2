package co.edu.ufps.legal_cases.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import co.edu.ufps.legal_cases.security.filter.JwtAuthenticationFilter;

@Configuration
@EnableMethodSecurity   //Para poder proteger endpoints por @PreAuthorize
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {}) // Agrega la configuracion de cors que tengo definida

                .csrf(csrf -> csrf.disable()) // Se desabilita porque la seguridad no va a ser por sesion sino por token jwt

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Cada peticion se valida con el token
                )

                .formLogin(form -> form.disable()) // Desabilita el loging que trae por defecto
                .httpBasic(basic -> basic.disable())    // Para que no traiga en los headers la autenticacion

                // Aqui le digo que apis son publicas y cuales no
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/me").authenticated()
                        .anyRequest().authenticated() // Le digo que de resto deben estar autenticados
                )

                // Esto es para que primero el filtro jwt valide el token y luego lo registra en el contexto de seguridad de spring
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}