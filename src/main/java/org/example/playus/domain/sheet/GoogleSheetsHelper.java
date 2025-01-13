package org.example.playus.domain.sheet;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.services.sheets.v4.Sheets;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class GoogleSheetsHelper {

    private Sheets getSheetsService() throws IOException, GeneralSecurityException {
        GoogleCredentials credentials;

        // Docker 환경 및 JAR 실행 시 classpath 기반으로 리소스를 가져오기
        InputStream resourceStream = getClass().getClassLoader().getResourceAsStream("googleSheet/google.json");
        if (resourceStream != null) {
            credentials = GoogleCredentials.fromStream(resourceStream)
                    .createScoped(Collections.singletonList(SheetsScopes.SPREADSHEETS));
        } else {
            // 로컬 환경에서는 FileInputStream으로 파일 시스템 경로를 사용
            System.out.println("Classpath 리소스를 찾을 수 없어 로컬 파일 시스템 경로 사용");
            credentials = GoogleCredentials.fromStream(new FileInputStream("src/main/resources/googleSheet/google.json"))
                    .createScoped(Collections.singletonList(SheetsScopes.SPREADSHEETS));
        }

        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("My Application")
                .build();
    }

    public String readCell(String spreadsheetId, String range) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
        return response.getValues().get(0).get(0).toString();
    }

    public List<List<Object>> readSheetData(String spreadsheetId, String range) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
        return response.getValues();
    }

    public void updateRow(String spreadsheetId, String range, List<Object> row) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        ValueRange body = new ValueRange().setValues(Collections.singletonList(row));
        service.spreadsheets().values().update(spreadsheetId, range, body).setValueInputOption("RAW").execute();
    }

    public void appendRow(String spreadsheetId, String range, List<Object> row) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        ValueRange body = new ValueRange().setValues(Collections.singletonList(row));
        service.spreadsheets().values().append(spreadsheetId, range, body).setValueInputOption("RAW").execute();
    }

    public void updateSheetData(String spreadsheetId, String range, List<List<Object>> data) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        ValueRange body = new ValueRange().setValues(data);
        service.spreadsheets().values().update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();
    }

    public void deleteRow(String spreadsheetId, String sheetName, int rowIndex) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();

        // 시트 ID 가져오기 (시트 이름을 기반으로)
        int sheetId = getSheetIdByName(spreadsheetId, sheetName);

        // 행 삭제 요청
        Request deleteRowRequest = new Request().setDeleteDimension(new DeleteDimensionRequest()
                .setRange(new DimensionRange()
                        .setSheetId(sheetId)  // 시트 ID
                        .setDimension("ROWS")  // 행 삭제
                        .setStartIndex(rowIndex - 1)  // 삭제할 행의 시작 인덱스 (0부터 시작, 따라서 -1)
                        .setEndIndex(rowIndex)  // 삭제할 행의 끝 인덱스 (1행 삭제)
                ));

        BatchUpdateSpreadsheetRequest batchRequest = new BatchUpdateSpreadsheetRequest()
                .setRequests(Collections.singletonList(deleteRowRequest));

        service.spreadsheets().batchUpdate(spreadsheetId, batchRequest).execute();

        log.info("Google Sheets에서 행 삭제 완료: sheetName={}, rowIndex={}", sheetName, rowIndex);
    }

    // 시트 이름 기반으로 시트 고유 ID 찾는 로직
    private int getSheetIdByName(String spreadsheetId, String sheetName) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();
        Spreadsheet spreadsheet = service.spreadsheets().get(spreadsheetId).execute();
        for (Sheet sheet : spreadsheet.getSheets()) {
            if (sheet.getProperties().getTitle().equals(sheetName)) {
                return sheet.getProperties().getSheetId();  // 시트 ID 반환
            }
        }
        throw new IllegalArgumentException("해당 이름의 시트를 찾을 수 없습니다: " + sheetName);
    }
}
