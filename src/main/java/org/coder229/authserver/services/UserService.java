package org.coder229.authserver.services;

import org.coder229.authserver.model.RegisterRequest;
import org.coder229.authserver.model.RegisterResponse;
import org.coder229.authserver.persistence.User;
import org.coder229.authserver.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
    @Value("${authservice.salt}")
    public String SALT;

    @Autowired
    private UserRepository userRepository;


    public RegisterResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.username())) {
            throw new DuplicateUserException("Username already used: " + registerRequest.username());
        }

        // TODO check password rules (lower, upper, number, special, 8 chars)

        String hashedPassword = BCrypt.hashpw(registerRequest.password(), SALT);
        User user = new User();
        user.setUsername(registerRequest.username());
        user.setPassword(hashedPassword);
        user.setEnabled(true);
        user.setVerified(false);
        userRepository.save(user);

        return new RegisterResponse(user.getUsername(), user.getEnabled(), user.getVerified());
    }

    public Page<User> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}
