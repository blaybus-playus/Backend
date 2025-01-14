package org.example.playus.domain.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.playus.domain.employee.Employee;
import org.example.playus.domain.security.service.UserDetailsImpl;
import org.example.playus.domain.user.dto.UserUpdateRequestDtoForAdmin;
import org.example.playus.domain.user.dto.UserUpdateRequestDtoForUser;
import org.example.playus.global.common.CommonResponse;
import org.example.playus.global.exception.CustomException;
import org.example.playus.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth")
public class UserController {

    private final UserService userService;

    @PutMapping("/admin/update/{id}")
    @Operation(summary = "관리자 권한으로 사용자 정보 업데이트", description = "관리자가 특정 사용자의 정보를 업데이트합니다.")
    public ResponseEntity<CommonResponse<Void>> updateUserAsAdmin(
            @PathVariable String id,
            @RequestBody UserUpdateRequestDtoForAdmin requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        if (!"ROLE_ADMIN".equals(userDetails.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new CommonResponse<>("접근 권한이 없습니다.", HttpStatus.FORBIDDEN.value(), null));
        }

        userService.updatePersonalInfoAsAdminById(id, requestDto);
        return ResponseEntity.ok(new CommonResponse<>("사용자 정보가 성공적으로 업데이트되었습니다.", HttpStatus.OK.value(), null));
    }

    @PutMapping("/update")
    public ResponseEntity<CommonResponse<Void>> updateUserAsUser(
            @RequestParam String username,
            @RequestBody UserUpdateRequestDtoForUser requestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        String loggedInUsername = userDetails.getUsername();

        if (!loggedInUsername.equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new CommonResponse<>("접근 권한이 없습니다.", HttpStatus.FORBIDDEN.value(), null));
        }

        // 현재 비밀번호 검증 및 업데이트 처리
        userService.updatePersonalInfoAsUser(loggedInUsername, requestDto.getCurrentPassword(), requestDto);
        return ResponseEntity.ok(new CommonResponse<>("사용자 정보가 성공적으로 업데이트되었습니다.", HttpStatus.OK.value(), null));
    }

    @DeleteMapping("/admin/delete/{id}")
    @Operation(summary = "회원 삭제", description = "관리자가 회원 계정을 삭제합니다.")
    public ResponseEntity<CommonResponse<Void>> deleteUser(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        // `UserDetailsImpl`을 통해 권한 확인
        if (!"ROLE_ADMIN".equals(userDetails.getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new CommonResponse<>("접근 권한이 없습니다.", HttpStatus.FORBIDDEN.value(), null));
        }
        userService.deleteUser(id);
        return ResponseEntity.ok(new CommonResponse<>("회원 계정이 성공적으로 삭제되었습니다.", HttpStatus.OK.value(), null));
    }
}