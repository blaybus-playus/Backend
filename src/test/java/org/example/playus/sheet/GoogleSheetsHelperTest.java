package org.example.playus.sheet;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
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
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class GoogleSheetsHelperTest {

    @InjectMocks
    private GoogleSheetsHelper googleSheetsHelper;

    @Mock
    private Sheets sheetsService;

    @Mock
    private Sheets.Spreadsheets spreadsheets;

    @Mock
    private Sheets.Spreadsheets.Values values;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        PowerMockito.whenNew(FileInputStream.class).withArguments(anyString()).thenReturn(mock(FileInputStream.class));
        when(sheetsService.spreadsheets()).thenReturn(spreadsheets);
        when(spreadsheets.values()).thenReturn(values);
    }

    @Test
    public void testReadSheetData() throws Exception {
        List<Object> mockData = List.of("data1", "data2", "data3");
        ValueRange mockValueRange = new ValueRange().setValues(List.of(mockData));
        when(values.get(anyString(), anyString()).execute()).thenReturn(mockValueRange);

        List<List<Object>> result = googleSheetsHelper.readSheetData("spreadsheetId", "range");
        assertEquals(List.of(mockData), result);
    }

    @Test
    public void testUpdateRow() throws Exception {
        List<Object> row = List.of("data1", "data2", "data3");
        ValueRange body = new ValueRange().setValues(Collections.singletonList(row));
        doNothing().when(values).update(anyString(), anyString(), eq(body)).setValueInputOption(anyString()).execute();

        googleSheetsHelper.updateRow("spreadsheetId", "range", row);

        verify(values, times(1)).update(anyString(), anyString(), eq(body));
    }
}
