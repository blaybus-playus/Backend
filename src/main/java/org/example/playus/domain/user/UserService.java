package org.example.playus.domain.user;

import lombok.RequiredArgsConstructor;
import org.example.playus.domain.employee.Account;
import org.example.playus.domain.employee.Employee;
import org.example.playus.domain.employee.EmployeeRepositoryMongo;
import org.example.playus.domain.employee.PersonalInfo;
import org.example.playus.domain.user.dto.UserUpdateRequestDtoForAdmin;
import org.example.playus.domain.user.dto.UserUpdateRequestDtoForUser;
import org.example.playus.global.exception.CustomException;
import org.example.playus.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final EmployeeRepositoryMongo employeeRepositoryMongo;

    public void updatePersonalInfoAsAdminById(String id, UserUpdateRequestDtoForAdmin requestDto) {
        Employee employee = employeeRepositoryMongo.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        PersonalInfo personalInfo = employee.getPersonalInfo();
        if (requestDto.getDepartment() != null) personalInfo.setDepartment(requestDto.getDepartment());
        if (requestDto.getJobGroup() != null) personalInfo.setJobGroup(requestDto.getJobGroup());
        if (requestDto.getLevel() != null) personalInfo.setLevel(requestDto.getLevel());

        employeeRepositoryMongo.save(employee);
    }

    public void updatePersonalInfoAsUser(String username, String currentPassword, UserUpdateRequestDtoForUser requestDto) {
        Employee employee = employeeRepositoryMongo.findByAccountUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 현재 비밀번호 검증
        String savedPassword = employee.getAccount().getUpdatedPassword() != null ?
                employee.getAccount().getUpdatedPassword() : employee.getAccount().getDefaultPassword();

        if (!currentPassword.equals(savedPassword)) {
            throw new CustomException(ErrorCode.PASSWORD_NOT_CORRECT);
        }

        // 새 비밀번호가 존재하면 업데이트
        String newPassword = requestDto.getNewPassword();
        if (newPassword != null && !newPassword.isBlank()) {
            employee.getAccount().setUpdatedPassword(newPassword);
        }

        // 캐릭터 ID 업데이트
        if (requestDto.getCharacterId() != null && !requestDto.getCharacterId().isBlank()) {
            employee.setCharacterId(requestDto.getCharacterId());
        }

        employeeRepositoryMongo.save(employee);
    }

    public void deleteUser(String id) {
        Employee employee = employeeRepositoryMongo.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        employeeRepositoryMongo.delete(employee);
    }
}