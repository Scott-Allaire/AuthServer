package org.coder229.authserver.controllers;

import jakarta.validation.Valid;
import org.coder229.authserver.model.RegisterRequest;
import org.coder229.authserver.model.RegisterResponse;
import org.coder229.authserver.persistence.User;
import org.coder229.authserver.services.UserService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@RestController
@RequestMapping(value = "/api/v1/register",
        produces = MediaType.APPLICATION_JSON_VALUE)
public class RegisterController {
    private static final Logger LOG = Logger.getLogger(RegisterController.class.getSimpleName());

    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping()
    public RegisterResponse register(@RequestBody @Valid RegisterRequest registerRequest) {
        LOG.info("Register request for " + registerRequest.username());
        User user = userService.register(registerRequest);
        return new RegisterResponse(user.getUsername(), user.getEnabled(), user.getVerified());
    }
}
