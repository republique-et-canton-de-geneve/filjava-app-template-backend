package ch.ge.filjava.apptemplatebackend.ui.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/services/**").authenticated()
                        .anyRequest().permitAll()
                )
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(withDefaults()))
                .build();
    }
}
