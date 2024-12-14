package org.coder229.authserver.controllers;

import org.coder229.authserver.model.PageResponse;
import org.coder229.authserver.model.UserResponse;
import org.coder229.authserver.persistence.Role;
import org.coder229.authserver.persistence.User;
import org.coder229.authserver.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping(value = "/api/v1/users",
        produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {
    private static final Logger LOG = Logger.getLogger(UserController.class.getSimpleName());

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // TODO change password USER AND ADMIN

    @GetMapping()
    @PreAuthorize("hasAuthority('ADMIN')")
    public PageResponse<UserResponse> userList(Pageable pageable) {
        // TODO make searchable
        Page<User> page = userService.getUsers(pageable);
        List<UserResponse> items = page.stream()
                .map(user -> {
                    List<String> roles = user.getRoles().stream().map(Role::getName).toList();
                    return new UserResponse(user.getId(), user.getUsername(), user.getEnabled(), user.getVerified(),
                            user.getExpires().toString(), roles);
                })
                .toList();
        return new PageResponse<>(page.getNumber(), page.getSize(), page.getTotalPages(),
                page.getTotalElements(), items);
    }

    // TODO update user details (except password)
    // TODO update user password
}
