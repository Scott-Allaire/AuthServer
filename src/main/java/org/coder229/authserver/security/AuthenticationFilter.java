package org.coder229.authserver.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.coder229.authserver.services.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;

public class AuthenticationFilter extends OncePerRequestFilter {

    private final AuthService authService;

    public AuthenticationFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.isNotBlank(authHeader)) {
            String token = StringUtils.removeStart(authHeader, "Bearer").trim();

            authService.validateToken(token).ifPresent(user -> {
                Collection<? extends GrantedAuthority> authorities = user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .toList();
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(user.getUsername(), token, authorities);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            });
        }

        filterChain.doFilter(request, response);
    }
}
