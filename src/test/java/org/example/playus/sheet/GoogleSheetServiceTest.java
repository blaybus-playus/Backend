package org.example.playus.sheet;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.example.playus.domain.employee.Employee;
import org.example.playus.domain.employee.EmployeeRepositoryMongo;
import org.example.playus.domain.sheet.GoogleSheetService;
import org.example.playus.domain.sheet.GoogleSheetsHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.FileInputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class GoogleSheetServiceTest {

    @Mock
    private EmployeeRepositoryMongo employeeRepositoryMongo;

    @Mock
    private GoogleSheetsHelper googleSheetsHelper;

    @InjectMocks
    private GoogleSheetService googleSheetService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetSheetData() throws Exception {
        List<Object> mockData = List.of("data1", "data2", "data3");
        ValueRange mockValueRange = new ValueRange().setValues(List.of(mockData));
        Sheets.Spreadsheets.Values mockValues = mock(Sheets.Spreadsheets.Values.class);
        Sheets mockSheets = mock(Sheets.class);
        when(mockValues.get(anyString(), anyString())).thenReturn(mock(Sheets.Spreadsheets.Values.Get.class));
        when(mockSheets.spreadsheets().values()).thenReturn(mockValues);
        when(mockValues.get(anyString(), anyString()).execute()).thenReturn(mockValueRange);

        GoogleCredential mockCredential = mock(GoogleCredential.class);
        when(mockCredential.getTransport()).thenReturn(mock(GoogleCredential.class).getTransport());
        when(mockCredential.getJsonFactory()).thenReturn(mock(GoogleCredential.class).getJsonFactory());
        when(mockCredential.createScoped(anyList())).thenReturn(mockCredential);

        PowerMockito.whenNew(FileInputStream.class).withArguments(anyString()).thenReturn(mock(FileInputStream.class));
        PowerMockito.mockStatic(GoogleCredential.class);
        when(GoogleCredential.fromStream(any(FileInputStream.class))).thenReturn(mockCredential);

        List<Object> result = googleSheetService.getSheetData("spreadsheetId", "range");
        assertEquals(mockData, result);
    }

    @Test
    public void testSyncGoogleSheetToMongo() throws Exception {
        List<List<Object>> mockSheetData = List.of(
                List.of("사번1", "이름1", "입사일1", "소속1", "직무그룹1", "레벨1", "아이디1", "기본패스워드1", "변경패스워드1"),
                List.of("사번2", "이름2", "입사일2", "소속2", "직무그룹2", "레벨2", "아이디2", "기본패스워드2", "변경패스워드2")
        );
        List<Employee> mockEmployees = List.of(new Employee(), new Employee());
        when(googleSheetsHelper.readSheetData(anyString(), anyString())).thenReturn(mockSheetData);
        when(employeeRepositoryMongo.saveAll(anyList())).thenReturn(mockEmployees);

        googleSheetService.syncGoogleSheetToMongo("spreadsheetId", "range");

        verify(googleSheetsHelper, times(1)).readSheetData(anyString(), anyString());
        verify(employeeRepositoryMongo, times(1)).saveAll(anyList());
    }

    @Test
    public void testSyncGoogleSheetToMongo_Exception() throws Exception {
        when(googleSheetsHelper.readSheetData(anyString(), anyString())).thenThrow(new RuntimeException("Test Exception"));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            googleSheetService.syncGoogleSheetToMongo("spreadsheetId", "range");
        });

        assertEquals("Error while syncing Google Sheets to MongoDB: Test Exception", exception.getMessage());
    }
}