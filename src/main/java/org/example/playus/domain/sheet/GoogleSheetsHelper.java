package org.example.playus.domain.sheet;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.services.sheets.v4.Sheets;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class GoogleSheetsHelper {

    private Sheets getSheetsService() throws IOException, GeneralSecurityException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("src/main/resources/googleSheet/google.json"))
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/spreadsheets"));
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

    public void deleteRow(String spreadsheetId, String range) throws IOException, GeneralSecurityException {
        Sheets service = getSheetsService();

        // 데이터 비우기 위한 빈 리스트 생성
        List<List<Object>> emptyRow = List.of(List.of("", "", ""));  // 비어 있는 셀 값

        log.info("Google Sheets 행 비우기 요청: range={}", range);

        // 빈 값으로 해당 범위를 업데이트하여 행 비우기
        ValueRange body = new ValueRange().setValues(emptyRow);
        service.spreadsheets().values().update(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();

        log.info("Google Sheets 행 비우기 요청 완료: range={}", range);
    }
}
