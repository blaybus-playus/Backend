package org.example.playus.domain.employee;

import lombok.RequiredArgsConstructor;
import org.example.playus.domain.employeeExp.EmployeeExp;
import org.example.playus.domain.employeeExp.EmployeeExpRepository;
import org.example.playus.domain.level.Level;
import org.example.playus.domain.level.LevelExp;
import org.example.playus.domain.level.LevelRepository;
import org.example.playus.global.exception.CustomException;
import org.example.playus.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);
    private final EmployeeRepositoryMongo employeeRepository;
    private final EmployeeExpRepository employeeExpRepository;
    private final LevelRepository levelRepository;


    public EmployeeExpReponseDto getEmployeeExp(int employeeId) {
        Employee employee = employeeRepository.findById(String.valueOf(employeeId))
                .orElseThrow(() -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND));

        EmployeeExp employeeExp = employeeExpRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND));

        // 레벨 그룹을 찾기 위해 Employee의 레벨에서 첫 글자 추출
        String levelGroupInitial = employee.getPersonalInfo().getLevel().substring(0, 1);

        Level level = levelRepository.findByLevelGroup(levelGroupInitial)
                .orElseThrow(() -> new CustomException(ErrorCode.LEVEL_NOT_FOUND));

        // 현재 레벨
        String currentLevel = employee.getPersonalInfo().getLevel();

        // 현재 레벨의 인덱스를 찾기
        List<LevelExp> levelExpList = level.getLevelExp();
        int currentLevelIndex = -1;
        for (int i = 0; i < levelExpList.size(); i++) {
            if (levelExpList.get(i).getLevel().equals(currentLevel)) {
                currentLevelIndex = i;
                break;
            }
        }

        if (currentLevelIndex == -1) {
            throw new CustomException(ErrorCode.LEVEL_NOT_FOUND);
        }

        // 다음 레벨 경험치 계산
        int nextLevelExp = 0;
        if (currentLevelIndex + 1 < levelExpList.size()) {
            nextLevelExp = levelExpList.get(currentLevelIndex + 1).getExp() -
                    levelExpList.get(currentLevelIndex).getExp();
        }

        // Employee.points를 이용하여 totalExp 계산
        int totalExp = 0;
        for(int point : employee.getPoints().values()) {
            totalExp += point;
        }

        return EmployeeExpReponseDto.builder()
                .name(employee.getPersonalInfo().getName())
                .employeeId(Integer.parseInt(employee.getEmployeeId()))
                .affiliation(employee.getPersonalInfo().getDepartment())
                .characterId(employee.getCharacterId())
                .level(employee.getPersonalInfo().getLevel())
                .thisYearExp(employeeExp.getExpForYear().getTotalExp())
                .totalExp(totalExp)
                .nextLevelExp(nextLevelExp)
                .build();
    }
}
