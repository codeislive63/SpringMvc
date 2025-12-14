package ru.codeislive63.springmvc.web;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.codeislive63.springmvc.domain.entity.Route;
import ru.codeislive63.springmvc.domain.entity.Station;
import ru.codeislive63.springmvc.domain.entity.Ticket;
import ru.codeislive63.springmvc.domain.entity.Train;
import ru.codeislive63.springmvc.domain.entity.Trip;
import ru.codeislive63.springmvc.domain.entity.UserAccount;
import ru.codeislive63.springmvc.repository.TripRepository;
import ru.codeislive63.springmvc.service.AdminService;
import ru.codeislive63.springmvc.service.BookingService;
import ru.codeislive63.springmvc.service.UserService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Тест 11: Удаление рейса при наличии активных билетов.
 * Ожидаем запрет удаления и сохранение целостности данных.
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class AdminTripDeleteIntegrityMockMvcTest {

    @Autowired MockMvc mockMvc;
    @Autowired AdminService adminService;
    @Autowired BookingService bookingService;
    @Autowired UserService userService;
    @Autowired TripRepository tripRepository;

    private Trip trip;

    @BeforeEach
    void setUp() {
        UserAccount customer = userService.getByEmail("customer@example.com");

        Station origin = adminService.createStation("DEL1", "Станция Удаления 1");
        Station dest = adminService.createStation("DEL2", "Станция Удаления 2");
        Train train = adminService.createTrain("DEL-TRAIN", "Поезд Удаления", 1);
        Route route = adminService.createRoute(origin.getId(), dest.getId(), 20, "Маршрут Удаления");
        trip = adminService.createTrip(
                route.getId(),
                train.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2),
                BigDecimal.valueOf(15)
        );

        // Создаём активный билет (BOOKED)
        Ticket t = bookingService.bookTicket(customer.getId(), trip.getId());
        if (t.getId() == null) {
            throw new IllegalStateException("Не удалось создать тестовый билет");
        }
    }

    @Test
    @WithUserDetails("admin@example.com")
    void deleteTripWithActiveTickets_shouldBeRejected() throws Exception {
        mockMvc.perform(post("/admin/panel/trips/{id}/delete", trip.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/panel/trips"))
                .andExpect(flash().attribute("error", Matchers.containsString("Нельзя удалить рейс")));

        // Рейс должен остаться в БД
        org.junit.jupiter.api.Assertions.assertTrue(
                tripRepository.findById(trip.getId()).isPresent(),
                "Рейс не должен удаляться при наличии активных билетов"
        );
    }
}
