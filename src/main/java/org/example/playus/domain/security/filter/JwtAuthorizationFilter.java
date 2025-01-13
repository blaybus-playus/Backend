package org.example.playus.domain.security.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.playus.domain.admin.Role;
import org.example.playus.domain.employee.Employee;
import org.example.playus.domain.employee.EmployeeRepositoryMongo;
import org.example.playus.domain.security.jwt.JwtUtil;
import org.example.playus.domain.security.service.UserDetailsImpl;
import org.example.playus.global.exception.CustomException;
import org.example.playus.global.exception.ErrorCode;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j(topic = "JwtAuthorizationFilter")
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final EmployeeRepositoryMongo employeeRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = JwtUtil.getJwtTokenFromHeader(request);

        if (StringUtils.hasText(token)) {
            try {
                JwtUtil.validateToken(token);  // JWT 검증
                Claims userInfo = JwtUtil.getUserInfoFromToken(token);
                String username = userInfo.getSubject();  // username 가져오기
                String role = (String) userInfo.get("role");  // role 가져오기
                log.info("토큰에서 가져온 사용자: {}, 권한: {}", username, role);

                setAuthentication(username, role);  // SecurityContextHolder에 인증 설정
            } catch (CustomException e) {
                handleTokenException(response, e);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void setAuthentication(String username, String role) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(username, role);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    private Authentication createAuthentication(String username, String role) {
        Employee employee = employeeRepository.findByAccountUsername(username).orElseThrow(
                () -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND)
        );

        // 권한을 JWT에서 가져온 값으로 설정
        UserDetailsImpl userDetailsImpl = new UserDetailsImpl(employee);

        // 기존 권한을 SimpleGrantedAuthority로 변환 후 새로운 권한 추가
        List<SimpleGrantedAuthority> updatedAuthorities = userDetailsImpl.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthority()))
                .distinct()
                .collect(Collectors.toList());  // 기존 권한을 SimpleGrantedAuthority 리스트로 변환


        log.info("인증된 사용자: {}, 설정된 권한: {}", userDetailsImpl.getUsername(), updatedAuthorities);

        return new UsernamePasswordAuthenticationToken(userDetailsImpl, null, updatedAuthorities);
    }

    private void handleTokenException(HttpServletResponse response, CustomException e) throws IOException {
        response.setStatus(e.getStatusCode().value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(e.getMessage());
    }

    private void updateAccessToken(HttpServletResponse response, String token) throws IOException {
        String username = JwtUtil.getUserInfoFromToken(token).getSubject();

        // 사용자 조회
        Employee employee = employeeRepository.findByAccountUsername(username).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );

        // 관리자 여부 확인
        Role role = employee.getAdmin() != null ? employee.getAdmin().getRole() : Role.ROLE_USER;

        // RefreshToken 가져오기
        String refreshToken = employee.getTokenStore().getRefreshToken();  // TokenStore에서 refreshToken 가져오기

        if (refreshToken != null && JwtUtil.isTokenValid(refreshToken)) {
            // 새로운 AccessToken 생성
            String newAccessToken = JwtUtil.createToken(employee.getAccount().getUsername(), role, JwtUtil.ACCESS_TOKEN_EXPIRATION);
            response.addHeader(JwtUtil.AUTHORIZATION_HEADER, "Bearer " + newAccessToken);
            responseSetting(response, 200, "AccessToken이 갱신되었습니다.");
        } else {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\": \"Refresh 토큰이 유효하지 않거나 만료되었습니다.\"}");
        }
    }

    private void responseSetting(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\": \"" + message + "\"}");
    }
}
