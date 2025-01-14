package org.example.playus.domain.user;

import lombok.RequiredArgsConstructor;
import org.example.playus.domain.employee.model.Account;
import org.example.playus.domain.employee.model.Employee;
import org.example.playus.domain.employee.EmployeeRepositoryMongo;
import org.example.playus.domain.employee.model.PersonalInfo;
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

    public void updatePersonalInfoAsUser(String username, UserUpdateRequestDtoForUser requestDto) {
        Employee employee = employeeRepositoryMongo.findByAccountUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Account account = employee.getAccount();
        String newPassword = requestDto.getUpdatedPassword();

        if (newPassword != null && !newPassword.isBlank()) {
            if (account.getUpdatedPassword() == null || account.getUpdatedPassword().isBlank()) {
                account.setUpdatedPassword(newPassword);
            } else {
                account.setDefaultPassword(account.getUpdatedPassword());
                account.setUpdatedPassword(newPassword);
            }
        }

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