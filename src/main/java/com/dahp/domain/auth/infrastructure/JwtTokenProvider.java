package com.dahp.domain.auth.infrastructure;

import com.dahp.domain.auth.controller.dto.LoginResponse;
import com.dahp.domain.auth.exception.AuthenticationException;
import com.dahp.domain.user.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLE = "role";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final SecretKey secretKey;
    private final Duration accessTokenValidity;
    private final Duration refreshTokenValidity;

    public JwtTokenProvider(
            @Value("${dahp.jwt.secret}") String secret,
            @Value("${dahp.jwt.access-token-validity}") Duration accessTokenValidity,
            @Value("${dahp.jwt.refresh-token-validity}") Duration refreshTokenValidity) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidity = accessTokenValidity;
        this.refreshTokenValidity = refreshTokenValidity;
    }

    public LoginResponse issueTokens(User user) {
        String accessToken = createAccessToken(user);
        String refreshToken = createRefreshToken(user);
        return new LoginResponse(
                accessToken,
                refreshToken,
                accessTokenValidity.toSeconds(),
                LoginResponse.UserInfo.from(user)
        );
    }

    public Long parseUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    public Long parseRefreshTokenUserId(String token) {
        Claims claims = parseClaims(token);
        if (!TYPE_REFRESH.equals(claims.get(CLAIM_TYPE))) {
            throw AuthenticationException.invalidToken();
        }
        return Long.valueOf(claims.getSubject());
    }

    public boolean validateAccessToken(String token) {
        try {
            Claims claims = parseClaims(token);
            return TYPE_ACCESS.equals(claims.get(CLAIM_TYPE));
        } catch (ExpiredJwtException e) {
            log.debug("Access token expired: {}", e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid access token: {}", e.getMessage());
            return false;
        }
    }

    private String createAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim(CLAIM_EMAIL, user.getEmail())
                .claim(CLAIM_ROLE, user.getRole().name())
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenValidity)))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    private String createRefreshToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(refreshTokenValidity)))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw AuthenticationException.tokenExpired();
        } catch (JwtException | IllegalArgumentException e) {
            throw AuthenticationException.invalidToken();
        }
    }
}
