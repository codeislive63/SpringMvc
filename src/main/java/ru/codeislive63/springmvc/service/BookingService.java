package ru.codeislive63.springmvc.service;

import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.codeislive63.springmvc.domain.PaymentStatus;
import ru.codeislive63.springmvc.domain.TicketStatus;
import ru.codeislive63.springmvc.domain.entity.*;
import ru.codeislive63.springmvc.repository.PaymentRepository;
import ru.codeislive63.springmvc.repository.TicketRepository;
import ru.codeislive63.springmvc.service.pricing.PriceStrategyFactory;
import ru.codeislive63.springmvc.web.dto.BookingRequest;
import ru.codeislive63.springmvc.web.dto.SeatView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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

        List<Integer> freeSeats = availableSeats(tripId);

        if (freeSeats.isEmpty())
        {
            throw new IllegalStateException("Нет свободных мест");
        }
        int nextSeat = freeSeats.getFirst();
        return getTicket(trip, user, nextSeat);
    }

    @NonNull
    private Ticket getTicket(Trip trip, UserAccount user, int nextSeat) {
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

    /**
     * Returns the list of seat numbers that are available for booking on the given trip.
     * Seats that are either booked or paid (i.e. statuses {@link TicketStatus#BOOKED} or {@link TicketStatus#PAID})
     * are excluded from the result.
     *
     * @param tripId ID of the trip
     * @return list of free seat numbers starting from 1 up to the train's seat capacity
     */
    public List<Integer> availableSeats(Long tripId) {
        Trip trip = tripService.getTrip(tripId);

        int capacity = Math.min(trip.getTrain().getSeatCapacity(), 50);
        List<TicketStatus> reservedStatuses = List.of(TicketStatus.BOOKED, TicketStatus.PAID);
        List<Integer> booked = ticketRepository.findBookedSeats(tripId, reservedStatuses);
        List<Integer> free = new ArrayList<>();
        for (int i = 1; i <= capacity; i++) {
            if (!booked.contains(i)) {
                free.add(i);
            }
        }
        return free;
    }

    /**
     * Возвращает схему вагона: список мест с указанием доступности.
     */
    public List<SeatView> seatMap(Long tripId) {
        Trip trip = tripService.getTrip(tripId);
        int capacity = Math.min(trip.getTrain().getSeatCapacity(), 50);
        List<Integer> free = availableSeats(tripId);

        List<SeatView> seats = new ArrayList<>(capacity);
        for (int i = 1; i <= capacity; i++) {
            seats.add(new SeatView(i, free.contains(i)));
        }
        return seats;
    }

    /**
     * Books a specific seat on a trip for the given user.
     * If the seat is not available or out of range, an exception will be thrown.
     *
     * @param userId     ID of the user
     * @param tripId     ID of the trip
     * @param seatNumber desired seat number
     * @return booked ticket
     */
    @Transactional
    public Ticket bookSeat(Long userId, Long tripId, int seatNumber) {
        Trip trip = tripService.getTrip(tripId);
        UserAccount user = userService.getUser(userId);

        int capacity = Math.min(trip.getTrain().getSeatCapacity(), 50);

        if (seatNumber < 1 || seatNumber > capacity) {
            throw new IllegalArgumentException("Неверный номер места");
        }

        if (trip.getSeatsAvailable() <= 0) {
            throw new IllegalStateException("Нет свободных мест");
        }

        List<Integer> available = availableSeats(tripId);
        if (!available.contains(seatNumber)) {
            throw new IllegalStateException("Место уже занято");
        }

        return getTicket(trip, user, seatNumber);
    }

    @Transactional
    public Ticket bookSeat(Long userId, Long tripId, int seatNumber, BookingRequest request) {
        Trip trip = tripService.getTrip(tripId);
        UserAccount user = userService.getUser(userId);

        int capacity = Math.min(trip.getTrain().getSeatCapacity(), 50);

        if (seatNumber < 1 || seatNumber > capacity) {
            throw new IllegalArgumentException("Неверный номер места");
        }

        if (trip.getSeatsAvailable() <= 0) {
            throw new IllegalStateException("Нет свободных мест");
        }

        List<Integer> available = availableSeats(tripId);
        if (!available.contains(seatNumber)) {
            throw new IllegalStateException("Место уже занято");
        }

        Ticket ticket = getTicket(trip, user, seatNumber);
        enrichWithPassenger(ticket, request);
        enrichWithServices(ticket, request);
        ticket.setPrice(applyDiscounts(ticket.getPrice(), request));
        return ticketRepository.save(ticket);
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

    private void enrichWithPassenger(Ticket ticket, BookingRequest request) {
        if (request == null) {
            return;
        }
        PassengerDetails details = ticket.getPassengerDetails() != null ? ticket.getPassengerDetails() : new PassengerDetails();
        details.setFullName(request.getPassengerName());
        details.setDocumentNumber(request.getPassengerDocument());
        details.setBenefitType(resolveBenefitLabel(request.getBenefitType()));
        details.setChildTicket(request.isChildTicket());
        details.setLoyaltyNumber(request.getLoyaltyNumber());
        ticket.setPassengerDetails(details);
    }

    private void enrichWithServices(Ticket ticket, BookingRequest request) {
        if (request == null) {
            return;
        }
        AdditionalServices services = ticket.getAdditionalServices() != null ? ticket.getAdditionalServices() : new AdditionalServices();
        services.setMealOption(request.getMealOption());
        services.setInsuranceIncluded(request.isInsuranceIncluded());
        services.setTransferIncluded(request.isTransferIncluded());
        services.setBaggageSelected(request.isBaggageSelected());
        services.setPetTravel(request.isPetTravel());
        ticket.setAdditionalServices(services);
    }

    private BigDecimal applyDiscounts(BigDecimal base, BookingRequest request) {
        if (request == null) {
            return base;
        }
        BigDecimal multiplier = BigDecimal.ONE;
        if (request.isChildTicket()) {
            multiplier = new BigDecimal("0.50");
        } else if (request.getBenefitType() != null && !request.getBenefitType().isBlank()) {
            multiplier = new BigDecimal("0.80");
        }
        return base.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }

    private String resolveBenefitLabel(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return switch (raw) {
            case "student" -> "Студент";
            case "pensioner" -> "Пенсионер";
            case "military" -> "Военнослужащий";
            default -> raw;
        };
    }
}
