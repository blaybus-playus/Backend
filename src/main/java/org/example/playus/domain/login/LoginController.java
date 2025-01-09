package org.example.playus.domain.login;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.playus.global.common.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name ="Longin", description = "LonginController APIs")
public class LoginController {
    private final LoginService loginService;

    @GetMapping("/login")
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
