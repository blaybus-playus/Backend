package org.example.playus.domain.user;

import lombok.RequiredArgsConstructor;

import org.example.playus.domain.employee.model.Employee;
import org.example.playus.domain.employee.EmployeeRepositoryMongo;
import org.example.playus.domain.employee.model.PersonalInfo;
import org.example.playus.domain.user.dto.UserProfileResponseDto;
import org.example.playus.domain.user.dto.UserUpdateCharacterRequestDtoUser;
import org.example.playus.domain.user.dto.UserUpdateRequestDtoForAdmin;
import org.example.playus.domain.user.dto.UserUpdatePasswordRequestDtoForUser;
import org.example.playus.global.exception.CustomException;
import org.example.playus.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final EmployeeRepositoryMongo employeeRepositoryMongo;

    public List<UserProfileResponseDto> getAllUsers() {
        List<Employee> employees = employeeRepositoryMongo.findAll();  // 모든 사용자 조회

        // Employee 엔티티를 DTO 리스트로 변환
        return employees.stream()
                .map(employee -> new UserProfileResponseDto(
                        employee.getEmployeeId(),
                        employee.getAccount().getUsername(),
                        employee.getPersonalInfo().getName(),
                        employee.getPersonalInfo().getJoinDate(),
                        employee.getPersonalInfo().getDepartment(),
                        employee.getPersonalInfo().getJobGroup(),
                        employee.getPersonalInfo().getLevel(),
                        employee.getCharacterId()
                ))
                .collect(Collectors.toList());
    }

    public List<UserProfileResponseDto> searchUsersByName(String name) {
        List<Employee> employees = employeeRepositoryMongo.findByPersonalInfoNameContainingIgnoreCase(name);

        if (employees.isEmpty()) {
            throw new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND);
        }

        // Employee → UserProfileResponseDto 변환
        return employees.stream()
                .map(employee -> new UserProfileResponseDto(
                        employee.getEmployeeId(),
                        employee.getAccount().getUsername(),  // 아이디(username)
                        employee.getPersonalInfo().getName(),  // 이름
                        employee.getPersonalInfo().getJoinDate(),
                        employee.getPersonalInfo().getDepartment(),
                        employee.getPersonalInfo().getJobGroup(),
                        employee.getPersonalInfo().getLevel(),
                        employee.getCharacterId()
                ))
                .collect(Collectors.toList());
    }

    public UserProfileResponseDto getUserProfileByUsername(String username) {
        Employee employee = employeeRepositoryMongo.findByAccountUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND));

        return new UserProfileResponseDto(
                employee.getEmployeeId(),
                employee.getAccount().getUsername(),  // 아이디(username)
                employee.getPersonalInfo().getName(),  // 이름
                employee.getPersonalInfo().getJoinDate(),
                employee.getPersonalInfo().getDepartment(),
                employee.getPersonalInfo().getJobGroup(),
                employee.getPersonalInfo().getLevel(),
                employee.getCharacterId()
        );
    }
    
    public void updatePersonalInfoAsAdminById(String id, UserUpdateRequestDtoForAdmin requestDto) {
        Employee employee = employeeRepositoryMongo.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        PersonalInfo personalInfo = employee.getPersonalInfo();
        if (requestDto.getDepartment() != null) personalInfo.setDepartment(requestDto.getDepartment());
        if (requestDto.getJobGroup() != null) personalInfo.setJobGroup(requestDto.getJobGroup());
        if (requestDto.getLevel() != null) personalInfo.setLevel(requestDto.getLevel());

        employeeRepositoryMongo.save(employee);
    }

    public void updatePasswordInfoAsUser(String username, UserUpdatePasswordRequestDtoForUser requestDto) {
        Employee employee = employeeRepositoryMongo.findByAccountUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 현재 비밀번호 검증
        String savedPassword = employee.getAccount().getUpdatedPassword() != null
                ? employee.getAccount().getUpdatedPassword()
                : employee.getAccount().getDefaultPassword();

        if (!requestDto.getCurrentPassword().equals(savedPassword)) {
            throw new CustomException(ErrorCode.PASSWORD_NOT_CORRECT);
        }

        // 새 비밀번호 검증 및 업데이트
        if (requestDto.getNewPassword() != null && !requestDto.getNewPassword().isBlank()) {
            employee.getAccount().setUpdatedPassword(requestDto.getNewPassword());
        } else {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        employeeRepositoryMongo.save(employee);
    }

    public void updateCharacterInfoAsUser(String username, UserUpdateCharacterRequestDtoUser requestDto) {
        Employee employee = employeeRepositoryMongo.findByAccountUsername(username)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (requestDto.getCharacterId() != null && !requestDto.getCharacterId().isBlank()) {
            employee.setCharacterId(requestDto.getCharacterId());
        } else {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        employeeRepositoryMongo.save(employee);
    }

    public void deleteUser(String id) {
        Employee employee = employeeRepositoryMongo.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        employeeRepositoryMongo.delete(employee);
    }
}