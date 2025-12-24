package ru.codeislive63.springmvc.web;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.codeislive63.springmvc.domain.entity.Route;
import ru.codeislive63.springmvc.domain.entity.Station;
import ru.codeislive63.springmvc.domain.entity.Train;
import ru.codeislive63.springmvc.service.AdminService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Тесты сценария поиска маршрутов (Тест 5 и Тест 6 из главы 3).
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class TripSearchMockMvcTest {

    @Autowired MockMvc mockMvc;
    @Autowired AdminService adminService;

    private Route route;
    private LocalDate tripDate;

    @BeforeEach
    void setUp() {
        Station origin = adminService.createStation("SRCH1", "Станция Поиска 1");
        Station dest = adminService.createStation("SRCH2", "Станция Поиска 2");
        Train train = adminService.createTrain("SRCH-TRAIN", "Поезд для поиска", 10);
        route = adminService.createRoute(origin.getId(), dest.getId(), 15, "Маршрут для поиска");

        tripDate = LocalDate.now().plusDays(1);
        LocalDateTime dep = tripDate.atTime(10, 0);
        LocalDateTime arr = tripDate.atTime(12, 0);

        adminService.createTrip(route.getId(), train.getId(), dep, arr, BigDecimal.valueOf(12));
    }

    /**
     * Тест 5: Поиск маршрутов по станциям и дате.
     */
    @Test
    void searchRoutes_found_shouldReturnResultsPageWithItineraries() throws Exception {
        mockMvc.perform(get("/routes/search")
                        .param("fromPointId", route.getOrigin().getId().toString())
                        .param("toPointId", route.getDestination().getId().toString())
                        .param("departureDate", tripDate.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/routes/results"))
                .andExpect(model().attributeExists("itineraries"))
                .andExpect(model().attribute("itineraries", Matchers.not(Matchers.empty())))
                .andExpect(content().string(Matchers.containsString("Результаты поиска")));
    }

    /**
     * Тест 6: Поиск при отсутствии подходящих рейсов.
     */
    @Test
    void searchRoutes_notFound_shouldReturnResultsPageWithEmptyListAndMessage() throws Exception {
        LocalDate noTripsDate = tripDate.plusDays(30);

        mockMvc.perform(get("/routes/search")
                        .param("fromPointId", route.getOrigin().getId().toString())
                        .param("toPointId", route.getDestination().getId().toString())
                        .param("departureDate", noTripsDate.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("pages/routes/results"))
                .andExpect(model().attributeExists("itineraries"))
                .andExpect(model().attribute("itineraries", Matchers.empty()))
                .andExpect(content().string(Matchers.containsString("Ничего не найдено")));
    }
}
