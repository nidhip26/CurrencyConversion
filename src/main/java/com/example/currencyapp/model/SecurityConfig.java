package com.example.currencyapp.model;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.http.HttpMethod;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/error", "/webjars/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/accounts").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/accounts").authenticated()
                .requestMatchers(HttpMethod.PUT, "/accounts").authenticated()
                .requestMatchers(HttpMethod.GET, "/accounts").authenticated()
                .requestMatchers(HttpMethod.GET, "/users").authenticated()
                .requestMatchers(HttpMethod.POST, "/users").authenticated()

                .requestMatchers(HttpMethod.GET, "/currencies").authenticated()
                .requestMatchers(HttpMethod.POST, "/currencies").authenticated()
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable())
            .oauth2Login()
            .and()
            .logout(logout -> logout.logoutSuccessUrl("/").permitAll());

        return http.build();
    }
}




