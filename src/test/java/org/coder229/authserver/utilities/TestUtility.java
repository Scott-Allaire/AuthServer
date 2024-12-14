package org.coder229.authserver.utilities;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import org.coder229.authserver.config.ServiceConfig;
import org.coder229.authserver.model.TokenType;
import org.coder229.authserver.model.UserRole;
import org.coder229.authserver.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class TestUtility {
    @Autowired
    private ServiceConfig serviceConfig;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private TokenRepository tokenRepository;

    public Token createToken(User user, TokenType tokenType, Instant expires) {
        String value = tokenType.equals(TokenType.REFRESH) ?
                UUID.randomUUID().toString() :
                createJwt(user, expires);
        Token token = new Token();
        token.setValue(value);
        token.setType(tokenType);
        token.setUser(user);
        token.setExpires(expires);
        return tokenRepository.save(token);
    }

    private String createJwt(User user, Instant expires) {
        Algorithm hs256 = Algorithm.HMAC256(serviceConfig.getJwtSecret());

        JWTCreator.Builder builder = JWT.create();
        builder.withIssuer("testutility");
        builder.withExpiresAt(new Date(expires.getEpochSecond() * 1000));
        builder.withSubject(user.getUsername());

        return builder.sign(hs256);
    }

    public User getOrCreateUser(String username, String password, List<UserRole> roles) {
        return userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User user = new User();
                    user.setUsername(username);
                    user.setPassword(BCrypt.hashpw(password, serviceConfig.getSalt()));
                    user.setEnabled(true);
                    user.setVerified(true);
                    user.setExpires(LocalDateTime.now().plusMinutes(15).toInstant(ZoneOffset.UTC));
                    user.setRoles(getRoles(roles));
                    return userRepository.save(user);
                });
    }

    private Set<Role> getRoles(List<UserRole> roles) {
        return roles.stream()
                .map(Enum::name)
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseGet(() -> {
                            Role role = new Role();
                            role.setName(roleName);
                            return roleRepository.save(role);
                        }))
                .collect(Collectors.toSet());
    }

    public void deleteAllUsers() {
        userRepository.findAll().forEach(user -> {
            tokenRepository.deleteAllByUser(user);
        });
        userRepository.deleteAll();
    }
}
