package org.coder229.authserver.config;

import org.coder229.authserver.model.RegisterRequest;
import org.coder229.authserver.persistence.User;
import org.coder229.authserver.services.UserService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalConfig implements ApplicationRunner {
    public static final String PASSWORD = "pass@word1";

    private UserService userService;

    public LocalConfig(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userService.findByUsername("admin").isEmpty()) {
            User admin = userService.register(new RegisterRequest("admin", PASSWORD));
            userService.addRole(admin.getId(), "ADMIN");
        }
        if (userService.findByUsername("user").isEmpty()) {
            userService.register(new RegisterRequest("user", PASSWORD));
        }
    }
}
