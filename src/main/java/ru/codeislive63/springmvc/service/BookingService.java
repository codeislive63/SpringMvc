package ru.codeislive63.springmvc.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.codeislive63.springmvc.domain.PaymentStatus;
import ru.codeislive63.springmvc.domain.TicketStatus;
import ru.codeislive63.springmvc.domain.entity.Payment;
import ru.codeislive63.springmvc.domain.entity.Ticket;
import ru.codeislive63.springmvc.domain.entity.Trip;
import ru.codeislive63.springmvc.domain.entity.UserAccount;
import ru.codeislive63.springmvc.repository.PaymentRepository;
import ru.codeislive63.springmvc.repository.TicketRepository;
import ru.codeislive63.springmvc.service.pricing.PriceStrategyFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final TicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;
    private final TripService tripService;
    private final UserService userService;
    private final PriceStrategyFactory priceStrategyFactory;

    @Transactional
    public Ticket bookTicket(Long userId, Long tripId) {
        Trip trip = tripService.getTrip(tripId);
        UserAccount user = userService.getUser(userId);

        if (trip.getSeatsAvailable() <= 0) {
            throw new IllegalStateException("Нет свободных мест");
        }

        int nextSeat = trip.getTrain().getSeatCapacity() - trip.getSeatsAvailable() + 1;
        BigDecimal price = priceStrategyFactory.chooseFor(trip).calculate(trip, 1);

        trip.setSeatsAvailable(trip.getSeatsAvailable() - 1);
        Ticket ticket = new Ticket();
        ticket.setTrip(trip);
        ticket.setUser(user);
        ticket.setSeatNumber(nextSeat);
        ticket.setPrice(price);
        ticket.setBookedAt(LocalDateTime.now());
        ticket.setStatus(TicketStatus.BOOKED);

        Ticket saved = ticketRepository.save(ticket);
        tripService.save(trip);
        return saved;
    }

    @Transactional
    public Payment payTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Билет не найден"));
        ticket.setStatus(TicketStatus.PAID);
        Payment payment = new Payment();
        payment.setTicket(ticket);
        payment.setAmount(ticket.getPrice());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setProcessedAt(OffsetDateTime.now());
        ticketRepository.save(ticket);
        return paymentRepository.save(payment);
    }

    @Transactional
    public Ticket cancel(Long ticketId, Long userId) {
        Ticket ticket = ticketRepository.findByIdAndUser(ticketId, userService.getUser(userId))
                .orElseThrow(() -> new IllegalArgumentException("Билет не найден у пользователя"));

        if (ticket.getStatus() == TicketStatus.PAID) {
            ticket.setStatus(TicketStatus.REFUNDED);
        } else {
            ticket.setStatus(TicketStatus.CANCELLED);
        }

        Trip trip = ticket.getTrip();
        trip.setSeatsAvailable(trip.getSeatsAvailable() + 1);
        tripService.save(trip);
        return ticketRepository.save(ticket);
    }

    public List<Ticket> myTickets(Long userId) {
        UserAccount user = userService.getUser(userId);
        return ticketRepository.findByUser(user);
    }
}


