package com.readour.common.security;

import com.readour.common.config.JwtProperties;
import com.readour.common.entity.User;
import com.readour.common.enums.ErrorCode;
import com.readour.common.exception.CustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.getSecret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(User user) {
        return buildToken(user, jwtProperties.getAccessTokenExpirationMillis());
    }

    private String buildToken(User user, long validityMillis) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(validityMillis);

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim("email", user.getEmail())
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            if (!StringUtils.hasText(token)) {
                return false;
            }
            parser().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRED, "만료된 토큰입니다.");
        } catch (Exception ex) {
            throw new CustomException(ErrorCode.INVALID_TOKEN, "유효하지 않은 토큰입니다.");
        }
    }

    public Long getUserId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    public LocalDateTime getExpiration(String token) {
        Date expiration = parseClaims(token).getExpiration();
        return LocalDateTime.ofInstant(expiration.toInstant(), ZoneId.systemDefault());
    }

    private JwtParser parser() {
        return Jwts.parser().verifyWith(key).build();
    }

    private Claims parseClaims(String token) {
        try {
            return parser().parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException ex) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRED, "만료된 토큰입니다.");
        } catch (Exception ex) {
            throw new CustomException(ErrorCode.INVALID_TOKEN, "유효하지 않은 토큰입니다.");
        }
    }

}
