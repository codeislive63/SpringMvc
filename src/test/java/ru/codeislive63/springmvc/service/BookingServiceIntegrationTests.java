package ru.codeislive63.springmvc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.codeislive63.springmvc.domain.PaymentStatus;
import ru.codeislive63.springmvc.domain.TicketStatus;
import ru.codeislive63.springmvc.domain.entity.Payment;
import ru.codeislive63.springmvc.domain.entity.Route;
import ru.codeislive63.springmvc.domain.entity.Station;
import ru.codeislive63.springmvc.domain.entity.Ticket;
import ru.codeislive63.springmvc.domain.entity.Train;
import ru.codeislive63.springmvc.domain.entity.Trip;
import ru.codeislive63.springmvc.domain.entity.UserAccount;
import ru.codeislive63.springmvc.repository.TripRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Интеграционные тесты ключевой бизнес-логики бронирования.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceIntegrationTests {

    @Autowired BookingService bookingService;
    @Autowired AdminService adminService;
    @Autowired UserService userService;
    @Autowired TripRepository tripRepository;

    private UserAccount customer;
    private Trip tripWithOneSeat;

    @BeforeEach
    void setUp() {
        customer = userService.getByEmail("customer@example.com");

        Station origin = adminService.createStation("TST1", "Тестовая Станция 1");
        Station destination = adminService.createStation("TST2", "Тестовая Станция 2");
        Train train = adminService.createTrain("TEST-TRAIN-ONE", "Тестовый поезд (1 место)", 1);
        Route route = adminService.createRoute(origin.getId(), destination.getId(), 10, "Тестовый маршрут");
        tripWithOneSeat = adminService.createTrip(
                route.getId(),
                train.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(1),
                BigDecimal.valueOf(10)
        );

        assertEquals(1, tripWithOneSeat.getSeatsAvailable());
    }

    @Test
    void bookTicket_shouldDecreaseSeatsAvailableAndCreateTicket() {
        Ticket ticket = bookingService.bookTicket(customer.getId(), tripWithOneSeat.getId());

        assertNotNull(ticket.getId());
        assertEquals(TicketStatus.BOOKED, ticket.getStatus());
        assertEquals(1, ticket.getSeatNumber());

        Trip reloaded = tripRepository.findById(tripWithOneSeat.getId()).orElseThrow();
        assertEquals(0, reloaded.getSeatsAvailable(), "После бронирования seatsAvailable должен уменьшиться");
    }

    @Test
    void bookTicket_whenNoSeats_shouldThrow() {
        bookingService.bookTicket(customer.getId(), tripWithOneSeat.getId());

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> bookingService.bookTicket(customer.getId(), tripWithOneSeat.getId())
        );
        assertTrue(ex.getMessage().toLowerCase().contains("нет свободных мест"));
    }

    @Test
    void cancelBookedTicket_shouldIncreaseSeatsAvailableAndSetCancelled() {
        Ticket ticket = bookingService.bookTicket(customer.getId(), tripWithOneSeat.getId());
        Trip afterBook = tripRepository.findById(tripWithOneSeat.getId()).orElseThrow();
        assertEquals(0, afterBook.getSeatsAvailable());

        Ticket cancelled = bookingService.cancel(ticket.getId(), customer.getId());
        assertEquals(TicketStatus.CANCELLED, cancelled.getStatus());

        Trip afterCancel = tripRepository.findById(tripWithOneSeat.getId()).orElseThrow();
        assertEquals(1, afterCancel.getSeatsAvailable(), "После отмены seatsAvailable должен увеличиться");
    }

    @Test
    void payTicket_shouldCreatePaymentAndSetPaid() {
        Ticket ticket = bookingService.bookTicket(customer.getId(), tripWithOneSeat.getId());

        Payment payment = bookingService.payTicket(ticket.getId());
        assertNotNull(payment.getId());
        assertEquals(PaymentStatus.SUCCESS, payment.getStatus());
        assertEquals(ticket.getId(), payment.getTicket().getId());

        Ticket refunded = bookingService.cancel(ticket.getId(), customer.getId());
        assertEquals(TicketStatus.REFUNDED, refunded.getStatus(), "Оплаченный билет при отмене должен стать REFUNDED");
    }
}
