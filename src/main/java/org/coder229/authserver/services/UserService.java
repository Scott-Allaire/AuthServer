package org.coder229.authserver.services;

import org.coder229.authserver.config.ServiceConfig;
import org.coder229.authserver.model.RegisterRequest;
import org.coder229.authserver.persistence.Role;
import org.coder229.authserver.persistence.RoleRepository;
import org.coder229.authserver.persistence.User;
import org.coder229.authserver.persistence.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class UserService {
    public static final String USER_ROLE = "USER";
    private final ServiceConfig serviceConfig;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(ServiceConfig serviceConfig, UserRepository userRepository, RoleRepository roleRepository) {
        this.serviceConfig = serviceConfig;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public User register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.username())) {
            throw new DuplicateUserException("Username already used: " + registerRequest.username());
        }

        // TODO check password rules (lower, upper, number, special, 8 chars)

        String hashedPassword = BCrypt.hashpw(registerRequest.password(), serviceConfig.getSalt());
        Instant expires = LocalDateTime.now()
                .plusMonths(serviceConfig.getPasswordExpiration()).toInstant(ZoneOffset.UTC);

        User user = new User();
        user.setUsername(registerRequest.username());
        user.setPassword(hashedPassword);
        user.setEnabled(true);
        user.setVerified(false);
        user.setExpires(expires);
        user.setRoles(Set.of(getRole(USER_ROLE)));
        return userRepository.save(user);
    }

    public Page<User> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void addRole(Long userId, String roleName) {
        userRepository.findById(userId).ifPresentOrElse(
                user -> user.getRoles().add(getRole(roleName)),
                () -> {
                    throw new UserNotFound("User not found: userId=" + userId);
                });
    }

    private Role getRole(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new RoleNotFound("Role not found: role=" + roleName));
    }
}
