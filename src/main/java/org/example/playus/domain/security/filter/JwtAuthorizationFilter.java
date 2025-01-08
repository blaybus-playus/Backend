package org.example.playus.domain.security.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.playus.domain.employee.Employee;
import org.example.playus.domain.employee.EmployeeRepositoryMongo;
import org.example.playus.domain.security.jwt.JwtUtil;
import org.example.playus.domain.security.service.UserDetailsImpl;
import org.example.playus.global.exception.CustomException;
import org.example.playus.global.exception.ErrorCode;
import org.example.playus.global.exception.TokenError;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j(topic = "JwtAuthorizationFilter")
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final EmployeeRepositoryMongo employeeRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = JwtUtil.getJwtTokenFromHeader(request);

        if (StringUtils.hasText(token)) {
            checkTokenzAndErrorHanding(response, token);

            Claims userInfo = JwtUtil.getUserInfoFromToken(token);

            try {
                setAuthentication(userInfo.getSubject());  // username 기반으로 인증 설정
            } catch (Exception e) {
                log.error(e.getMessage());
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Authentication authentication = createAuthentication(username);
        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);
    }

    private Authentication createAuthentication(String username) {
        Employee employee = employeeRepository.findByAccountUsername(username).orElseThrow(
                () -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND)
        );

        UserDetailsImpl userDetailsImpl = new UserDetailsImpl(employee);
        return new UsernamePasswordAuthenticationToken(userDetailsImpl, null, userDetailsImpl.getAuthorities());
    }

    private void checkTokenzAndErrorHanding(HttpServletResponse response, String token) throws IOException {
        switch (JwtUtil.validateToken(token)) {
            case VALID:
                break;
            case EXPRIED:
                updateAccessToken(response, token);
                return;
            default:
                response.setStatus(401);
                response.setContentType("text/plain;charset=UTF-8");
                response.getWriter().write("유효하지 않은 토큰입니다.");
                return;
        }

        checkBlacklist(token);
    }

    private void checkBlacklist(String token) {
        String username = JwtUtil.getUserInfoFromToken(token).getSubject();
        Employee employee = employeeRepository.findByAccountUsername(username).orElseThrow(
                () -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND)
        );

        if (Objects.isNull(employee.getAccount().getUpdatedPassword())) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    private void updateAccessToken(HttpServletResponse response, String token) throws IOException {
        String username = JwtUtil.getUserInfoFromToken(token).getSubject();
        Employee employee = employeeRepository.findByAccountUsername(username).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );

        String refresh = employee.getAccount().getUpdatedPassword().substring(7);
        if (TokenError.VALID == JwtUtil.validateToken(refresh)) {
            String newToken = JwtUtil.createToken(employee.getAccount().getUsername(), JwtUtil.ACCESS_TOKEN_EXPIRATION);
            response.addHeader(JwtUtil.AUTHORIZATION_HEADER, newToken);
        } else if (TokenError.EXPRIED == JwtUtil.validateToken(refresh)) {
            response.setStatus(401);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("refresh 토큰이 만료되었습니다.");
        } else {
            throw new RuntimeException("refresh token이 유효하지 않습니다.");
        }
    }
}
