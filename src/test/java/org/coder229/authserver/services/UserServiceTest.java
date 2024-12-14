package org.coder229.authserver.services;

import org.coder229.authserver.persistence.RoleRepository;
import org.coder229.authserver.persistence.User;
import org.coder229.authserver.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void addRoleWhenUserNotFound() {
        assertThrows(UserNotFound.class, () -> {
            userService.addRole(123L, "TESTER");
        });
    }

    @Test
    void addRoleWhenRoleNotFound() {
        User user = new User();
        user.setId(123L);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThrows(RoleNotFound.class, () -> {
            userService.addRole(123L, "TESTER");
        });
    }
}