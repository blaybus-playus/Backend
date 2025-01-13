package org.example.playus.domain.signup;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.playus.domain.security.service.UserDetailsImpl;
import org.example.playus.domain.signup.dto.SignupRequestDto;
import org.example.playus.domain.signup.dto.SignupResponseDto;
import org.example.playus.global.common.CommonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth")
public class SignupController {
    private final SignupService signupService;

    @PostMapping("/signup")
    @Operation(summary = "signup", description = "회원가입 기능")
    public ResponseEntity<CommonResponse> signup(
            @RequestBody SignupRequestDto requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
            ) {

        if (!"ROLE_ADMIN".equals(userDetails.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new CommonResponse<>("접근 권한이 없습니다.", HttpStatus.FORBIDDEN.value(), null));
        }

        SignupResponseDto responseDto = signupService.signup(requestDto);
        CommonResponse response = new CommonResponse<>("회원가입 성공", 200, responseDto);
        return ResponseEntity.ok(response);
    }
}
