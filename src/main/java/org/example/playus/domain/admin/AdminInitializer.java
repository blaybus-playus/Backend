package org.example.playus.domain.admin;


import lombok.RequiredArgsConstructor;
import org.example.playus.domain.employee.EmployeeRepositoryMongo;
import org.example.playus.domain.employee.model.Account;
import org.example.playus.domain.employee.model.Employee;
import org.example.playus.domain.employee.model.PersonalInfo;
import org.example.playus.domain.employee.model.RecentExpDetail;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {
    private final EmployeeRepositoryMongo employeeRepositoryMongo;

    @Override
    public void run(String... args) throws Exception {
        if (employeeRepositoryMongo.findAll().isEmpty()) {
            Employee adminEmployee = new Employee();
            adminEmployee.setEmployeeId("admin-1");
            adminEmployee.setCharacterId("man1");

            PersonalInfo personalInfo = new PersonalInfo();
            personalInfo.setName("admin");
            personalInfo.setJoinDate("admin");
            personalInfo.setDepartment("admin");
            personalInfo.setJobGroup("admin");
            personalInfo.setLevel("admin");

            adminEmployee.setPersonalInfo(personalInfo);


            Account account = Account.builder()
                    .username("admin")
                    .defaultPassword("adminPassword")
                    .updatedPassword(null)
                    .build();
            adminEmployee.setAccount(account);

            adminEmployee.setPoints(null);
            adminEmployee.setTokenStore(null);

            List<RecentExpDetail> recentExpDetailList = new ArrayList<>();

            adminEmployee.setRecentExpDetails(recentExpDetailList);

            Admin admin = new Admin();
            admin.setRole(Role.ROLE_ADMIN);
            adminEmployee.setAdmin(admin);
            employeeRepositoryMongo.save(adminEmployee);
        }
    }
}
