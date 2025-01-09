package org.example.playus.domain.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.playus.global.exception.TokenError;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import io.jsonwebtoken.security.SignatureException;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j(topic = "JwtUtil")
@RequiredArgsConstructor
@Component
public class JwtUtil {
    public static final Long ACCESS_TOKEN_EXPIRATION = 60 * 60 * 1000L; // 60분
    public static final Long REFRESH_TOKEN_EXPIRATION = 24 * 60 * 60 * 1000L; // 1일
    public static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private static final Key key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(System.getenv("JWT_SECRET_KEY")));

    public static String createToken(String username, Long expiresIn) {
        Date date = new Date(System.currentTimeMillis() + expiresIn * 1000);

        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

        return BEARER_PREFIX + Jwts.builder()
                .setSubject(username)
                .setExpiration(date)
                .setIssuedAt(date)
                .signWith(key)
                .compact();
    }

    public static String  getJwtTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public static TokenError validateToken(String token){
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return TokenError.VALID;
        } catch (SecurityException | MalformedJwtException | SignatureException e) {
            log.error("Invalid JWT signature, 유효하지 않는 JWT 서명 입니다.");
            return TokenError.INVALID_SIGN;
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token, 만료된 JWT token 입니다.");
            return TokenError.EXPRIED;
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.");
            return TokenError.UNSUPPORTED;
        } catch (IllegalArgumentException e) {
            log.error("JWT claims is empty, 잘못된 JWT 토큰 입니다.");
            return TokenError.EMPTY_CLAIMS;
        }
    }

    public static Claims getUserInfoFromToken(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}
