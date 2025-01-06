package domain.sheet;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/google")
@RequiredArgsConstructor
public class GoogleSheetController {

    private final GoogleSheetService googleSheetService;

    @Value("${google.spreadsheet.id}")
    private String spreadSheetId; // 스프레드시트 ID

    // TODO : 셀 범위를 수정해야 함
    private static final String RANGE = "요구사항!B2:D2"; // 읽을 셀 범위

    @GetMapping("/read")
    public ResponseEntity<List<Object>> readFromSheet() {
        try {
            List<Object> data = googleSheetService.getSheetData(spreadSheetId, RANGE);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
