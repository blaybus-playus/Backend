package org.example.playus.domain.logout;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.playus.domain.security.jwt.JwtUtil;
import org.example.playus.global.common.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Logout", description = "LogoutController APIs")
public class LogoutController {

    private final LogoutService logoutService;

    @Operation(summary = "logiut", description = "로그아웃 기능")
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse> logout(HttpServletRequest request) {
        String token = JwtUtil.getJwtTokenFromHeader(request);  // 헤더에서 토큰 추출
        String username = JwtUtil.getUserInfoFromToken(token).getSubject();  // 토큰에서 username 추출

        CommonResponse<String> response = logoutService.logout(username);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
