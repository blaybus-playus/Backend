package org.example.playus.sheet;

import org.example.playus.domain.sheet.GoogleSheetController;
import org.example.playus.domain.sheet.GoogleSheetService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GoogleSheetController.class)
public class GoogleSheetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GoogleSheetService googleSheetService;

    @Test
    public void testReadFromSheet() throws Exception {
        List<Object> mockData = List.of("data1", "data2", "data3");
        Mockito.when(googleSheetService.getSheetData(anyString(), anyString())).thenReturn(mockData);

        mockMvc.perform(get("/google/read"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[\"data1\", \"data2\", \"data3\"]"));
    }

    @Test
    public void testSyncUsers() throws Exception {
        Mockito.doNothing().when(googleSheetService).syncGoogleSheetToMongo(anyString(), anyString());

        mockMvc.perform(post("/google/sync"))
                .andExpect(status().isOk())
                .andExpect(content().string("데이터 동기화 완료"));
    }
}
