package org.coder229.authserver.config;

import org.coder229.authserver.security.AuthenticationFilter;
import org.coder229.authserver.services.AuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthService authService) throws Exception {
        // "/health", "/info", "/api/v1/login", "/api/v1/register"
        http.authorizeRequests()
                .requestMatchers("/health", "/info", "/api/v1/login", "/api/v1/register")
                .permitAll()
                .anyRequest()
                .authenticated()
            .and()
                .exceptionHandling()
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
                .addFilterBefore(new AuthenticationFilter(authService), UsernamePasswordAuthenticationFilter.class)
                .csrf().disable();
        return http.build();
    }
}
