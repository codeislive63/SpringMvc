package ru.codeislive63.springmvc.web;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ReportExportXlsxMvcTest {

    private static final String REPORT_URL = "/admin/dashboard/report.xlsx";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void exportReportXlsx_shouldReturnValidXlsx_forAdmin() throws Exception {
        MvcResult result = mockMvc.perform(
                        get(REPORT_URL)
                                .header("Accept", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                )
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("attachment")))
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andReturn();

        byte[] bytes = result.getResponse().getContentAsByteArray();
        assertThat(bytes).isNotNull();
        assertThat(bytes.length).isGreaterThan(100);

        try (XSSFWorkbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertThat(wb.getNumberOfSheets()).isGreaterThan(0);

            Sheet sheet0 = wb.getSheetAt(0);

            assertThat(sheet0).isNotNull();
            assertThat(sheet0.getPhysicalNumberOfRows()).isGreaterThan(0);
        }
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"CUSTOMER"})
    void exportReportXlsx_shouldBeForbidden_forNonAdmin() throws Exception {
        mockMvc.perform(get(REPORT_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    void exportReportXlsx_shouldRedirectToLogin_forAnonymous() throws Exception {
        mockMvc.perform(get(REPORT_URL))
                .andExpect(status().is3xxRedirection());
    }
}
