package org.coder229.authserver.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.transaction.Transactional;
import org.coder229.authserver.model.LoginResponse;
import org.coder229.authserver.model.TokenType;
import org.coder229.authserver.persistence.Token;
import org.coder229.authserver.persistence.TokenRepository;
import org.coder229.authserver.persistence.User;
import org.coder229.authserver.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    @Value("${authservice.issuer}")
    public String ISSUER;

    @Value("${authservice.secret}")
    public String SECRET;

    @Value("${authservice.salt}")
    public String SALT;

    @Value("${authservice.jwtDuration}")
    public Duration jwtDuration;

    @Value("${authservice.refreshDuration}")
    public Duration refreshDuration;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    public LoginResponse login(String username, String password) throws NotFoundException {
        String hashedPassword = BCrypt.hashpw(password, SALT);
        return userRepository.findByUsernameAndPassword(username, hashedPassword)
                .map(user -> {
                    // check to see if user is enabled and verified

                    Instant expiresAt = Instant.now().plus(jwtDuration);
                    Date expiresAtDate = new Date(expiresAt.getEpochSecond() * 1000);
                    Algorithm hs256 = Algorithm.HMAC256(SECRET);

                    JWTCreator.Builder builder = JWT.create();
                    builder.withIssuer(ISSUER);
                    builder.withExpiresAt(expiresAtDate);
                    builder.withSubject(user.getUsername());

                    String accessToken = builder.sign(hs256);
                    saveToken(user, accessToken, TokenType.ACCESS, expiresAt);
                    String refreshToken = UUID.randomUUID().toString();
                    saveToken(user, refreshToken, TokenType.REFRESH, expiresAt);

                    return new LoginResponse(accessToken, refreshToken, expiresAt);
                }).orElseThrow(() -> new UsernameNotFoundException("User/password not found: " + username));
    }

    private Token saveToken(User user, String value, TokenType type, Instant expiresAt) {
        Token token = new Token();
        token.setUser(user);
        token.setType(type);
        token.setValue(value);
        token.setExpires(expiresAt);
        return tokenRepository.save(token);
    }
}
