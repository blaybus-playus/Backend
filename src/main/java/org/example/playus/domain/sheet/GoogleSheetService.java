package org.example.playus.domain.sheet;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import jakarta.transaction.Transactional;
import org.example.playus.domain.employee.Employee;
import org.example.playus.domain.employee.EmployeeRepositoryMongo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Service
public class GoogleSheetService {
    private static final String APPLICATION_NAME = "Google Sheets API Example";
    private static final String CREDENTIALS_FILE_PATH = "src/main/resources/googleSheet/google.json"; // 서비스 계정 키 경로

    private static final GoogleSheetsHelper googleSheetsHelper = new GoogleSheetsHelper();
    private static final Logger log = LoggerFactory.getLogger(GoogleSheetService.class);

    @Autowired
    private EmployeeRepositoryMongo employeeRepositoryMongo;

    public List<Object> getSheetData(String spreadsheetId, String range) throws IOException, GeneralSecurityException {
        try {
            // GoogleCredential 생성
            GoogleCredential credential = GoogleCredential
                    .fromStream(new FileInputStream(CREDENTIALS_FILE_PATH))
                    .createScoped(List.of("https://www.googleapis.com/auth/spreadsheets"));

            // Sheets API 클라이언트 생성
            Sheets sheetsService = new Sheets.Builder(
                    credential.getTransport(),
                    credential.getJsonFactory(),
                    credential
            )
                    .setApplicationName(APPLICATION_NAME)
                    .build();

            // API 호출
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();

            // 응답 값 로그
            log.info("Google Sheets API Response: {}", response.getValues());

            return response.getValues() != null ? response.getValues().get(0) : List.of(); // 첫 번째 행 반환
        } catch (IOException e) {
            log.error("Error accessing Google Sheets API: ", e); // 예외 발생 시 로그 남기기
            throw new RuntimeException("Error accessing Google Sheets API: " + e.getMessage());
        }
    }

    @Transactional
    public void syncGoogleSheetToMongo(String spreadsheetId, String range) throws Exception {
        try {
            // Google Sheets에서 데이터 읽기
            List<List<Object>> sheetData = googleSheetsHelper.readSheetData(spreadsheetId, range);

            // 데이터 읽기 확인
            log.info("Sheet Data Read: {}", sheetData);

            // 데이터를 User 객체로 변환
            List<Employee> employees = GoogleSheetsConvert.convertToUsers(sheetData);
            for(Employee employee : employees) {
                System.out.println(employee);
            }

            // MongoDB에 저장 및 저장된 결과 확인
            List<Employee> savedEmployees = employeeRepositoryMongo.saveAll(employees);

            // 저장 성공 여부 확인
            if (savedEmployees.size() == employees.size()) {
                log.info("All users saved successfully.");
            } else {
                log.warn("Some users were not saved.");
            }
        } catch (Exception e) {
            log.error("Error while syncing Google Sheets to MongoDB: ", e); // 예외 발생 시 로그 남기기
            throw new RuntimeException("Error while syncing Google Sheets to MongoDB: " + e.getMessage());
        }
    }
}