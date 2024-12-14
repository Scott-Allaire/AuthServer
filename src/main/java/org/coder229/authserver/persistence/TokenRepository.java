package org.coder229.authserver.persistence;

import org.coder229.authserver.model.TokenType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    void deleteAllByUser(User user);

    Optional<Token> findByUserIdAndType(Long userId, TokenType type);

    Optional<Token> findByValueAndType(String value, TokenType type);

    void deleteByValueAndType(String value, TokenType type);
}
