package ru.codeislive63.springmvc.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.codeislive63.springmvc.repository.StationRepository;
import ru.codeislive63.springmvc.repository.TrainRepository;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тест 12: Редактирование справочников (станции/поезда).
 * Проверяем, что изменения сохраняются.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AdminDirectoriesCrudMockMvcTest {

    @Autowired MockMvc mockMvc;
    @Autowired StationRepository stationRepository;
    @Autowired TrainRepository trainRepository;

    @Test
    @WithUserDetails("admin@example.com")
    void adminCanCreateStation() throws Exception {
        mockMvc.perform(post("/admin/panel/stations/create")
                        .param("code", "CRD1")
                        .param("name", "Справочная станция"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/panel/stations"));

        assertTrue(stationRepository.findByCode("CRD1").isPresent(),
                "После создания станция должна появиться в БД");
    }

    @Test
    @WithUserDetails("admin@example.com")
    void adminCanCreateTrain() throws Exception {
        mockMvc.perform(post("/admin/panel/trains/create")
                        .param("code", "CRD-TRAIN")
                        .param("name", "Справочный поезд")
                        .param("seatCapacity", "50"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/panel/trains"));

        assertTrue(trainRepository.findByCode("CRD-TRAIN").isPresent(),
                "После создания поезд должен появиться в БД");
    }
}
