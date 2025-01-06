package domain.sheet;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Service
public class GoogleSheetService {
    private static final String APPLICATION_NAME = "Google Sheets API Example";
    private static final String CREDENTIALS_FILE_PATH = "src/main/resources/googleSheet/google.json"; // 서비스 계정 키 경로

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

            return response.getValues() != null ? response.getValues().get(0) : List.of(); // 첫 번째 행 반환
        } catch (IOException e) {
            e.printStackTrace();  // 예외 출력
            throw new RuntimeException("Error accessing Google Sheets API: " + e.getMessage());
        }
    }
}
