package org.example.playus.domain.employee;

import lombok.RequiredArgsConstructor;
import org.example.playus.domain.employee.dto.EmployeeExpDetailResponseDto;
import org.example.playus.domain.employee.dto.EmployeeExpReponseDto;
import org.example.playus.domain.employee.dto.EmployeeGroupQuestResponseDto;
import org.example.playus.domain.employee.dto.EmployeeHistoryResponseDto;
import org.example.playus.domain.employee.model.Employee;
import org.example.playus.domain.employee.model.RecentExpDetail;
import org.example.playus.domain.employeeExp.EmployeeExp;
import org.example.playus.domain.employeeExp.EmployeeExpRepository;
import org.example.playus.domain.level.Level;
import org.example.playus.domain.level.LevelExp;
import org.example.playus.domain.level.LevelRepository;
import org.example.playus.domain.quest.groupGuset.GroupQuest;
import org.example.playus.domain.quest.groupGuset.GroupQuestRepositoryMongo;
import org.example.playus.domain.quest.groupGuset.GroupQeustInfo;
import org.example.playus.domain.quest.leaderQuest.*;
import org.example.playus.global.exception.CustomException;
import org.example.playus.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeService {
    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);
    private final EmployeeRepositoryMongo employeeRepository;
    private final EmployeeExpRepository employeeExpRepository;
    private final GroupQuestRepositoryMongo groupQuestRepository;
    private final LevelRepository levelRepository;
    private final LeaderQuestExpRepository leaderQuestExpRepository;
    private final LeaderQuestRepository leaderQuestRepository;


    public EmployeeExpReponseDto getEmployeeExp(int employeeId) {
        Employee employee = employeeRepository.findById(String.valueOf(employeeId))
                .orElseThrow(() -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND));

        EmployeeExp employeeExp = employeeExpRepository.findByEmployeeIdAndYear(employeeId, 2024)
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
                .limitExp(employeeExp.getMaxExp() / 2)
                .build();
    }

    public EmployeeExpDetailResponseDto getEmployeeExpByYear(int employeeId, int year) {
        EmployeeExp employeeExp = employeeExpRepository.findByEmployeeIdAndYear(employeeId, year)
                .orElseThrow(() -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND));

        return EmployeeExpDetailResponseDto.builder()
                .name(employeeExp.getName())
                .employeeId(String.valueOf(employeeExp.getEmployeeId()))
                .fistHalfExp(employeeExp.getExpForYear().getFirstHalfEvaluationExp())
                .secondHalfExp(employeeExp.getExpForYear().getSecondHalfEvaluationExp())
                .groupQuestExp(employeeExp.getExpForYear().getGroupQuestExp())
                .leaderQuestExp(employeeExp.getExpForYear().getLeaderQuestExp())
                .projectExp(employeeExp.getExpForYear().getProjectExp())
                .build();
    }

    public List<EmployeeHistoryResponseDto> getEmployeeHistory(int employeeId) {
        Employee employee = employeeRepository.findById(String.valueOf(employeeId))
                .orElseThrow(() -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND));

        List<EmployeeHistoryResponseDto> responseDto = new ArrayList<>();

        for(RecentExpDetail history : employee.getRecentExpDetails()) {
            responseDto.add(EmployeeHistoryResponseDto.builder()
                    .employeeId(employee.getEmployeeId())
                    .employeeName(employee.getPersonalInfo().getName())
                    .date(history.getDate())
                    .questGroup(history.getQuestGroup())
                    .questName(history.getQuestName())
                    .score(history.getScore())
                    .build());
        }

        return responseDto;
    }

    public EmployeeGroupQuestResponseDto getEmployeeQuestGroup(int employeeId) {
        Employee employee = employeeRepository.findById(String.valueOf(employeeId))
                .orElseThrow(() -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND));

        String affiliation = employee.getPersonalInfo().getDepartment();
        int department = Integer.parseInt(employee.getPersonalInfo().getJobGroup());

        List<GroupQuest> groupQuestList = groupQuestRepository.findAllByAffiliationAndDepartment(affiliation, department);

        List<GroupQeustInfo> groupQeustInfoList = new ArrayList<>();
        for(GroupQuest groupQuest : groupQuestList) {
            groupQeustInfoList.add(GroupQeustInfo.builder()
                            .weekOrMonth(groupQuest.getGroupExperiences().getWeek())
                            .score(groupQuest.getGroupExperiences().getExperience())
                            .etc(groupQuest.getGroupExperiences().getEtc())
                    .build());
        }

        return EmployeeGroupQuestResponseDto.builder()
                .employeeId(employee.getEmployeeId())
                .employeeName(employee.getPersonalInfo().getName())
                .affiliation(affiliation)
                .department(department)
                .period(groupQuestList.get(0).getPeriod())
                .maxScore(groupQuestList.get(0).getMaxScore())
                .mediumScore(groupQuestList.get(0).getMediumScore())
                .groupQeustInfoList(groupQeustInfoList)
                .build();
    }

    public EmployeeLeaderQuestResponseDto getEmployeeQuestLeader(int employeeId) {
        Employee employee = employeeRepository.findById(String.valueOf(employeeId))
                .orElseThrow(() -> new CustomException(ErrorCode.EMPLOYEE_NOT_FOUND));

        String affiliation = employee.getPersonalInfo().getDepartment();

        List<LeaderQuestExp> leaderQuestExpList = leaderQuestExpRepository.findAllByAffiliation(affiliation);

        List<LeaderQuestInfo> leaderQuestInfoList = new ArrayList<>();

        for (LeaderQuestExp leaderQuestExp : leaderQuestExpList) {
            LeaderQuest leaderQuest = leaderQuestRepository.findByLeaderQuestList_QuestName(leaderQuestExp.getLeaderQuestEmployeeList().getQuestName());
            leaderQuestInfoList.add(LeaderQuestInfo.builder()
                            .questName(leaderQuestExp.getLeaderQuestEmployeeList().getQuestName())
                            .period(leaderQuest.getLeaderQuestList().getPeriod())
                            .weekOrMonth(leaderQuestExp.getLeaderQuestEmployeeList().getMonth())
                            .achievement(leaderQuestExp.getLeaderQuestEmployeeList().getAchievement())
                            .score(leaderQuestExp.getLeaderQuestEmployeeList().getScore())
                            .requireForMax(leaderQuest.getLeaderQuestList().getRequireForMax())
                            .requireForMedium(leaderQuest.getLeaderQuestList().getRequireForMedium())
                    .build());
        }

        return EmployeeLeaderQuestResponseDto.builder()
                .employeeId(employee.getEmployeeId())
                .employeeName(employee.getPersonalInfo().getName())
                .affiliation(affiliation)
                .leaderQuestInfoList(leaderQuestInfoList)
                .build();
    }
}
