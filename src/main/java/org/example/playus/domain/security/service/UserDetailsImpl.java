package org.example.playus.domain.security.service;

import org.example.playus.domain.employee.model.Employee;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserDetailsImpl implements UserDetails {
    private final Employee employee;
    private final String username;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Employee employee) {
        this.employee = employee;
        this.username = employee.getAccount().getUsername();
        this.password = employee.getAccount().getDefaultPassword();  // 기본 비밀번호 사용

        // Admin 필드에 따라 ROLE 설정
        String role = (employee.getAdmin() != null && employee.getAdmin().getRole() != null)
                ? employee.getAdmin().getRole().name()  // ROLE_ADMIN 또는 ROLE_USER 등
                : "ROLE_USER";  // 기본 권한 설정

        this.authorities = List.of(new SimpleGrantedAuthority(role));  // 권한 설정
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;  // 권한 목록 반환
    }

    @Override
    public String getPassword() {
        return password;  // 비밀번호 반환
    }

    @Override
    public String getUsername() {
        return username;  // 사용자명 반환
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // 계정 만료 여부 (true: 만료되지 않음)
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // 계정 잠김 여부 (true: 잠기지 않음)
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // 비밀번호 만료 여부 (true: 만료되지 않음)
    }

    @Override
    public boolean isEnabled() {
        return true;  // 계정 활성화 여부 (true: 활성화)
    }

    public String getRole() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");  // 기본 권한
    }


    public Employee getEmployee() {
        return employee;  // 사용자 정보 반환
    }
}
