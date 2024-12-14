package org.coder229.authserver.controllers;

import jakarta.validation.Valid;
import org.coder229.authserver.model.LoginRequest;
import org.coder229.authserver.model.LoginResponse;
import org.coder229.authserver.model.RefreshRequest;
import org.coder229.authserver.model.RefreshResponse;
import org.coder229.authserver.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;

@RestController
@RequestMapping(value = "/api/v1/auth",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {
    private static final Logger LOG = Logger.getLogger(AuthController.class.getSimpleName());

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest loginRequest) {
        LOG.info("Login request: username=" + loginRequest.username());
        return authService.login(loginRequest.username(), loginRequest.password());
    }

    @PostMapping("/refresh")
    public RefreshResponse refresh(@RequestBody @Valid RefreshRequest refreshRequest) {
        LOG.info("Refresh request: userId=" + refreshRequest.userId());
        return authService.refresh(refreshRequest);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        LOG.info("Logout request: username=" + auth.getPrincipal());
        String accessToken = authHeader.substring(authHeader.indexOf(' ')).trim();
        authService.logout(accessToken);
    }
}
