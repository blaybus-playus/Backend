package org.example.playus.domain.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.playus.domain.admin.Role;
import org.example.playus.global.exception.CustomException;
import org.example.playus.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j(topic = "JwtUtil")
@RequiredArgsConstructor
@Component
public class JwtUtil {
    public static final Long ACCESS_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000L; // 7일
    public static final Long REFRESH_TOKEN_EXPIRATION = 24 * 60 * 60 * 1000L; // 1일
    public static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private static final Key key;

    static {
        String secretKey = System.getenv("JWT_SECRET_KEY");
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalArgumentException("환경 변수 JWT_SECRET_KEY가 설정되지 않았습니다.");
        }
        key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
    }

    public static String createToken(String username, Role role, Long expiresIn) {
        Date date = new Date(System.currentTimeMillis() + expiresIn);

        Claims claims = Jwts.claims().setSubject(username);
        claims.put("role", role.getRole());  // 권한 추가 (예: ROLE_ADMIN)

        return BEARER_PREFIX + Jwts.builder()
                .setClaims(claims)
                .setExpiration(date)
                .setIssuedAt(new Date())
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public static String getJwtTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            // Authorization 헤더가 없을 경우 null 반환
            return null;
        }
        return bearerToken.substring(7);  // "Bearer " 제거 후 반환
    }

    public static void validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        } catch (SecurityException | MalformedJwtException | SignatureException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.TOKEN_EXPIRATION);
        } catch (UnsupportedJwtException e) {
            throw new CustomException(ErrorCode.NOT_SUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.FALSE_TOKEN);
        }
    }

    public static Claims getUserInfoFromToken(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            log.warn("Token expired for user: {}", e.getClaims().getSubject());
            return e.getClaims();
        }
    }

    public static boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;  // 토큰이 유효함
        } catch (JwtException | IllegalArgumentException e) {
            return false;  // 토큰이 유효하지 않음
        }
    }
}
