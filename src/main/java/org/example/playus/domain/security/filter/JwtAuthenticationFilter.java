package org.example.playus.domain.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.playus.domain.employee.Employee;
import org.example.playus.domain.employee.EmployeeRepositoryMongo;
import org.example.playus.domain.login.LoginRequestDto;
import org.example.playus.domain.security.jwt.JwtUtil;
import org.example.playus.global.exception.CustomException;
import org.example.playus.global.exception.ErrorCode;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Slf4j(topic = "JwtAuthenticationFilter")
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final EmployeeRepositoryMongo employeeRepository;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, EmployeeRepositoryMongo employeeRepository) {
        super.setAuthenticationManager(authenticationManager);
        this.employeeRepository = employeeRepository;

        setFilterProcessesUrl("/api/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            LoginRequestDto requestDto = new ObjectMapper().readValue(request.getInputStream(), LoginRequestDto.class);

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(requestDto.getUsername(), requestDto.getPassword(), null)
            );
        } catch (IOException e) {
            log.error("로그인 시 입력값 매핑 실패: {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        String username = ((UserDetails) authResult.getPrincipal()).getUsername();

        String accessToken = JwtUtil.createToken(username, JwtUtil.ACCESS_TOKEN_EXPIRATION);
        String refreshToken = JwtUtil.createToken(username, JwtUtil.REFRESH_TOKEN_EXPIRATION);

        saveRefreshToken(username, refreshToken);

        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, accessToken); // response header에 access token 넣기
        responseSetting(response, 200, "로그인에 성공하였습니다.");
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        log.error("로그인 실패: {}", failed.getMessage());
        throw new CustomException(ErrorCode.AUTHENTICATION_FAILED);
    }

    private void saveRefreshToken(String username, String refreshToken) {
        Employee employee = employeeRepository.findByAccountUsername(username).orElseThrow(
                () -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND)
        );
        employee.getAccount().setUpdatedPassword(refreshToken);
        employeeRepository.save(employee);
    }

    private void responseSetting(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(message);
    }
}