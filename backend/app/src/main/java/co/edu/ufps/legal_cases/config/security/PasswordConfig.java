package co.edu.ufps.legal_cases.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// Lo uso en servicios para encriptar las contraseñas antes de guardarlas en la base de datos y para verificar las contraseñas durante el proceso de autenticación. 
@Configuration
public class PasswordConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}