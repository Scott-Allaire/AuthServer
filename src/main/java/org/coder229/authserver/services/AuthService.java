package org.coder229.authserver.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import org.apache.commons.lang3.time.DateUtils;
import org.coder229.authserver.entities.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;

@Service
@Transactional
public class AuthService {

    @Value("${authservice.issuer}")
    public String ISSUER;

    @Value("${authservice.secret}")
    public String SECRET;

    @Value("${authservice.salt}")
    public String SALT;

    @Value("${authservice.valid-for-minutes}")
    public int validForMinutes;

    @Autowired
    private UserRepository userRepository;

    public String login(String username, String password) throws NotFoundException {
        return userRepository.findByUsernameAndPassword(username, BCrypt.hashpw(password, SALT))
                .map(user -> {
                    JWTCreator.Builder builder = JWT.create();
                    builder.withIssuer(ISSUER);
                    builder.withExpiresAt(DateUtils.addMinutes(new Date(), validForMinutes));
                    builder.withSubject(user.getUsername());

                    Algorithm hs256 = Algorithm.HMAC256(SECRET);
                    return builder.sign(hs256);
                }).orElseThrow(NotFoundException::new);
    }
}
