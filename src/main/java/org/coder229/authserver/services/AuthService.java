package org.coder229.authserver.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.transaction.Transactional;
import org.coder229.authserver.config.ServiceConfig;
import org.coder229.authserver.model.*;
import org.coder229.authserver.persistence.Token;
import org.coder229.authserver.persistence.TokenRepository;
import org.coder229.authserver.persistence.User;
import org.coder229.authserver.persistence.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
@Transactional
public class AuthService {
    private final ServiceConfig serviceConfig;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;

    public AuthService(ServiceConfig serviceConfig,
                       UserRepository userRepository,
                       TokenRepository tokenRepository) {
        this.serviceConfig = serviceConfig;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    public LoginResponse login(String username, String password) {
        String hashedPassword = BCrypt.hashpw(password, serviceConfig.getSalt());
        return userRepository.findByUsernameAndPassword(username, hashedPassword)
                .map(user -> {
                    // TODO check to see if user is enabled, verified and not expired
                    tokenRepository.deleteAllByUser(user);

                    Instant accessExpires = Instant.now().plus(serviceConfig.getJwtExpiration());
                    String accessToken = createAccessToken(user, accessExpires);
                    saveToken(user, accessToken, TokenType.ACCESS, accessExpires);

                    return new LoginResponse(user.getId(), accessToken, accessExpires);
                }).orElseThrow(() -> new UsernameNotFoundException("User/password not found: '" + username + "'"));
    }

    public RefreshResponse refresh(RefreshRequest refreshRequest) {
        return tokenRepository.findByUserIdAndType(refreshRequest.userId(), TokenType.ACCESS)
                .filter(token -> token.getExpires().isAfter(Instant.now()))
                .map( existingToken -> {
                    Instant accessExpires = Instant.now().plus(serviceConfig.getJwtExpiration());
                    String accessToken = createAccessToken(existingToken.getUser(), accessExpires);

                    return new RefreshResponse(accessToken, accessExpires);
                })
                .orElseThrow(() -> new BadCredentialsException("Token not found for user: " + refreshRequest.userId()));
    }

    public void logout(String accessToken) {
        tokenRepository.deleteByValueAndType(accessToken, TokenType.ACCESS);
    }

    private String createAccessToken(User user, Instant expiresAt) {
        Date expiresAtDate = new Date(expiresAt.getEpochSecond() * 1000);
        Algorithm hs256 = Algorithm.HMAC256(serviceConfig.getJwtSecret());

        JWTCreator.Builder builder = JWT.create();
        builder.withIssuer(serviceConfig.getJwtIssuer());
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
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(serviceConfig.getJwtSecret()))
                .build();
        DecodedJWT decoded = verifier.verify(accessToken);

        return tokenRepository.findByValueAndType(accessToken, TokenType.ACCESS)
                .flatMap(token -> {
                    return userRepository.findByUsername(decoded.getSubject());
                });
    }
}
