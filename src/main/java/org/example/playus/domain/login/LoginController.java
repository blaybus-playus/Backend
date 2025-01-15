package org.example.playus.domain.login;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.playus.domain.login.dto.LoginRequestDto;
import org.example.playus.domain.login.dto.LoginResponseDto;
import org.example.playus.global.common.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name ="Auth")
public class LoginController {
    private final LoginService loginService;

    @PostMapping("/login")
    @Operation(summary = "login", description = "로그인 기능")
    public ResponseEntity<CommonResponse> login(@RequestBody LoginRequestDto requestDto) {
        try {
            LoginResponseDto responseDto = loginService.login(requestDto);
            CommonResponse response = new CommonResponse<>("로그인 성공", 200, responseDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            CommonResponse response = new CommonResponse<>("로그인 실패", 500, null);
            return ResponseEntity.status(500).body(response);
        }
    }
}
