package org.example.playus.sheet;

import org.example.playus.domain.employee.Employee;
import org.example.playus.domain.sheet.GoogleSheetsConvert;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GoogleSheetsConvertTest {

    private static final Logger log = LoggerFactory.getLogger(GoogleSheetsConvertTest.class);

    @Test
    public void testConvertToUsers() {
        List<List<Object>> mockSheetData = List.of(
                List.of("사번", "이름", "입사일", "소속", "직무그룹", "레벨", "아이디", "기본패스워드", "변경패스워드", "2023년", "2022년", "2021년", "2020년", "2019년", "2018년", "2017년", "2016년", "2015년", "2014년", "2013년"),
                List.of("1", "김원기", "2020-01-01", "개발팀", "개발", "1", "hong", "1234", "abcd", "100", "200", "300", "400", "500", "600", "700", "800", "900", "1000", "1100")
        );

        List<Employee> employees = GoogleSheetsConvert.convertToUsers(mockSheetData);

        assertEquals(1, employees.size());

        Employee employee1 = employees.get(0);
        assertEquals("1", employee1.getEmployeeId());
        assertEquals("김원기", employee1.getPersonalInfo().getName());
        assertEquals("hong", employee1.getAccount().getUsername());
        assertEquals(100, (int) employee1.getPoints().get("2023"));
    }

    @Test
    public void testConvertToUsers_EmptySheet() {
        List<List<Object>> emptySheetData = List.of();

        List<Employee> employees = GoogleSheetsConvert.convertToUsers(emptySheetData);

        assertTrue(employees.isEmpty());
    }

    @Test
    public void testConvertToUsers_MissingHeader() {
        List<List<Object>> invalidSheetData = List.of(
                List.of("사번", "이름", "입사일", "소속", "직무그룹", "레벨", "아이디", "기본패스워드", "변경패스워드")
        );

        try {
            GoogleSheetsConvert.convertToUsers(invalidSheetData);
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }
}