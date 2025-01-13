package org.example.playus.domain.security.config;

import lombok.RequiredArgsConstructor;
import org.example.playus.domain.employee.EmployeeRepositoryMongo;
import org.example.playus.domain.security.filter.JwtAuthenticationFilter;
import org.example.playus.domain.security.filter.JwtAuthorizationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;


@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final EmployeeRepositoryMongo employeeRepositoryMongo;
    private final AuthenticationConfiguration authenticationConfiguration;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();  // 비밀번호 암호화 비활성화
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception {
        return new JwtAuthenticationFilter(authenticationManager(), employeeRepositoryMongo);
    }

    @Bean
    public JwtAuthorizationFilter jwtAuthorizationFilter() {
        return new JwtAuthorizationFilter(employeeRepositoryMongo);  // Authorization 필터 추가
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // CSRF 비활성화
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:8080", "http://34.122.87.147", "http://35.184.127.151"));  // 허용할 도메인
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);  // 인증정보 포함 허용 여부
                    return config;
                }))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Swagger 및 actuator 접근 허용
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        // 로그인 및 회원가입 관련 요청 허용
                        .requestMatchers("/auth/**").permitAll()  // "/auth/**" 모든 요청 허용 (컨트롤러에서 권한 처리)
                        // 게시판 관련 요청 허용
                        .requestMatchers("/board/**").permitAll()  // CRUD API 모두 허용 (컨트롤러에서 권한 처리)
                        // 관리자 관련 API 허용 (컨트롤러에서 권한 처리)
                        .requestMatchers("/admin/**").permitAll()
                        // 나머지 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // 필터 체인 등록
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthorizationFilter(), JwtAuthenticationFilter.class);  // Authorization 필터 등록

        return http.build();
    }
}