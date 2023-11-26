package org.coder229.authserver.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.transaction.Transactional;
import org.coder229.authserver.model.LoginResponse;
import org.coder229.authserver.model.RefreshRequest;
import org.coder229.authserver.model.RefreshResponse;
import org.coder229.authserver.model.TokenType;
import org.coder229.authserver.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

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

    @Autowired
    private RoleRepository roleRepository;

    public LoginResponse login(String username, String password) {
        String hashedPassword = BCrypt.hashpw(password, SALT);
        return userRepository.findByUsernameAndPassword(username, hashedPassword)
                .map(user -> {
                    // TODO check to see if user is enabled and verified
                    tokenRepository.deleteAllByUser(user);

                    Instant accessExpires = Instant.now().plus(jwtDuration);
                    String accessToken = createAccessToken(user, accessExpires);
                    saveToken(user, accessToken, TokenType.ACCESS, accessExpires);

                    Instant refreshExpires = Instant.now().plus(refreshDuration);
                    String refreshToken = UUID.randomUUID().toString();
                    saveToken(user, refreshToken, TokenType.REFRESH, refreshExpires);

                    return new LoginResponse(user.getId(), accessToken, refreshToken, accessExpires);
                }).orElseThrow(() -> new UsernameNotFoundException("User/password not found: '" + username + "'"));
    }

    public RefreshResponse refresh(RefreshRequest refreshRequest) {
        return tokenRepository.findByUserIdAndType(refreshRequest.userId(), TokenType.REFRESH)
                .filter(token -> token.getExpires().isAfter(Instant.now()))
                .map(refreshToken -> {
                    Instant refreshExpires = Instant.now().plus(refreshDuration);
                    refreshToken.setExpires(refreshExpires);

                    Instant accessExpires = Instant.now().plus(jwtDuration);
                    String accessToken = createAccessToken(refreshToken.getUser(), accessExpires);

                    return new RefreshResponse(accessToken, accessExpires);
                })
                .orElseThrow(() -> new BadCredentialsException("Token not found for user: " + refreshRequest.userId()));
    }

    private String createAccessToken(User user, Instant expiresAt) {
        Date expiresAtDate = new Date(expiresAt.getEpochSecond() * 1000);
        Algorithm hs256 = Algorithm.HMAC256(SECRET);

        JWTCreator.Builder builder = JWT.create();
        builder.withIssuer(ISSUER);
        builder.withExpiresAt(expiresAtDate);
        builder.withSubject(user.getUsername());

        return builder.sign(hs256);
    }

    private Token saveToken(User user, String value, TokenType type, Instant expiresAt) {
        Token token = new Token();
        token.setUser(user);
        token.setType(type);
        token.setValue(value);
        token.setExpires(expiresAt);
        return tokenRepository.save(token);
    }

    public Optional<User> validateToken(String accessToken) {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET))
                .build();
        DecodedJWT decoded = verifier.verify(accessToken);
        return userRepository.findByUsername(decoded.getSubject());
    }
}
