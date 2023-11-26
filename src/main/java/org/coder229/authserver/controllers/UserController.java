package org.coder229.authserver.controllers;

import jakarta.validation.Valid;
import org.coder229.authserver.model.PageResponse;
import org.coder229.authserver.model.RegisterRequest;
import org.coder229.authserver.model.RegisterResponse;
import org.coder229.authserver.model.UserResponse;
import org.coder229.authserver.persistence.Role;
import org.coder229.authserver.persistence.User;
import org.coder229.authserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping(value = "/api/v1",
        produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {
    private static final Logger LOG = Logger.getLogger(UserController.class.getSimpleName());

    @Autowired
    private UserService userService;

    @PostMapping(value="/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RegisterResponse register(@RequestBody @Valid RegisterRequest registerRequest) {
        LOG.info("Register request for " + registerRequest.username());
        return userService.register(registerRequest);
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public PageResponse<UserResponse> userList(Pageable pageable) {
        Page<User> page = userService.getUsers(pageable);
        List<UserResponse> items = page.stream()
                .map(user -> {
                    List<String> roles = user.getRoles().stream().map(Role::getName).toList();
                    return new UserResponse(user.getId(), user.getUsername(), user.getEnabled(),
                            user.getVerified(), roles);
                })
                .toList();
        return new PageResponse<>(page.getNumber(), page.getSize(), page.getTotalPages(),
                page.getTotalElements(), items);
    }
}
