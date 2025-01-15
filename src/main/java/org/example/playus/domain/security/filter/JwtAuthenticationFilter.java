package org.example.playus.domain.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.playus.domain.admin.Role;
import org.example.playus.domain.employee.EmployeeRepositoryMongo;
import org.example.playus.domain.employee.model.Employee;
import org.example.playus.domain.employee.model.TokenStore;
import org.example.playus.domain.login.dto.LoginRequestDto;
import org.example.playus.domain.login.dto.LoginResponseDto;
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
import java.io.PrintWriter;

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

        // 로그인한 사용자의 권한 정보 가져오기
        Employee employee = employeeRepository.findByAccountUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND));

        // 권한 확인 및 설정 (ROLE_USER 또는 ROLE_ADMIN)
        Role role = employee.getAdmin() != null ? employee.getAdmin().getRole() : Role.ROLE_USER;

        // AccessToken 및 RefreshToken 생성
        String accessToken = JwtUtil.createToken(username, role, JwtUtil.ACCESS_TOKEN_EXPIRATION);
        String refreshToken = JwtUtil.createToken(username, role, JwtUtil.REFRESH_TOKEN_EXPIRATION);

        saveRefreshToken(employee, refreshToken);  // RefreshToken 저장

        //반환하는값을 직접 노출하는것은 보안에 위험!!!
        boolean isAdmin = role.equals(Role.ROLE_ADMIN);

        LoginResponseDto loginResponseDto = LoginResponseDto.builder()
                .employeeId(employee.getEmployeeId().toString())
                .personalInfo(employee.getPersonalInfo())
                .account(employee.getAccount())
                .tokenStore(new TokenStore(accessToken, refreshToken))
                .isAdmin(isAdmin)
                .build();

        // Response Header 및 Body 설정
        response.addHeader(JwtUtil.AUTHORIZATION_HEADER, accessToken);  // AccessToken 추가
        responseSetting(response, 200, loginResponseDto);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        log.error("로그인 실패: {}", failed.getMessage());
        throw new CustomException(ErrorCode.AUTHENTICATION_FAILED);
    }

    private void saveRefreshToken(Employee employee, String refreshToken) {
        TokenStore tokenStore = TokenStore.builder()
                .accessToken(employee.getTokenStore().getAccessToken())  // 기존 AccessToken 유지
                .refreshToken(refreshToken)  // 새 RefreshToken 저장
                .build();
        employee.setTokenStore(tokenStore);
        employeeRepository.save(employee);  // 토큰 정보 업데이트
    }

    private void responseSetting(HttpServletResponse response, int statusCode, Object responseDto) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");

        // DTO 객체를 JSON 문자열로 변환
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(responseDto);

        PrintWriter out = response.getWriter();
        out.print(jsonResponse);  // JSON 응답 출력
        out.flush();
    }
}