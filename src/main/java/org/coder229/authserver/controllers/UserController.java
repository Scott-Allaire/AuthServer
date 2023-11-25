package org.coder229.authserver.controllers;

import jakarta.validation.Valid;
import org.coder229.authserver.model.RegisterRequest;
import org.coder229.authserver.model.RegisterResponse;
import org.coder229.authserver.services.UserService;
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
public class UserController {
    private static final Logger LOG = Logger.getLogger(UserController.class.getSimpleName());

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public RegisterResponse register(@RequestBody @Valid RegisterRequest registerRequest) {
        LOG.info("Register request for " + registerRequest.username());
        return userService.register(registerRequest);
    }

}
