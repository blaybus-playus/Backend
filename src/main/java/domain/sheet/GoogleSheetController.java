package domain.sheet;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/google")
@RequiredArgsConstructor
public class GoogleSheetController {

    private final GoogleSheetService googleSheetService;

    // TODO : ID ignore에 추가 필요
    private static final String SPREADSHEET_ID = "1TcVHmKbN0mWorIEdYdqVeunCgxEw2AOHjANaa6rvIX4"; // 스프레드시트 ID
    private static final String RANGE = "요구사항!B2:D2"; // 읽을 셀 범위

    @GetMapping("/read")
    public ResponseEntity<List<Object>> readFromSheet() {
        try {
            List<Object> data = googleSheetService.getSheetData(SPREADSHEET_ID, RANGE);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
