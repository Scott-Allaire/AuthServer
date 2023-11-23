package org.coder229.authserver.controllers;

import org.coder229.authserver.model.LoginRequest;
import org.coder229.authserver.model.LoginResponse;
import org.coder229.authserver.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@RestController
@RequestMapping(value = "/api/v1",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {
    private static final Logger LOG = Logger.getLogger(AuthController.class.getSimpleName());

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        LOG.info("Login request for " + loginRequest.username());
        return authService.login(loginRequest.username(), loginRequest.password());
    }
}
