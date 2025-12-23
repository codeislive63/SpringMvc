package ru.codeislive63.springmvc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.codeislive63.springmvc.domain.TicketStatus;
import ru.codeislive63.springmvc.domain.entity.Trip;
import ru.codeislive63.springmvc.repository.TicketRepository;
import ru.codeislive63.springmvc.repository.TripRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TripRepository tripRepository;
    private final TicketRepository ticketRepository;

    /** Заполняемость по каждому рейсу: считаем занятые места и делим на вместимость. */
    public List<TripOccupancy> getTripOccupancies() {
        List<Trip> allTrips = tripRepository.findAll();
        List<TripOccupancy> result = new ArrayList<>();
        for (Trip trip : allTrips) {
            int capacity = trip.getTrain().getSeatCapacity();
            long booked = ticketRepository.countByTripIdAndStatusIn(
                    trip.getId(),
                    List.of(TicketStatus.BOOKED, TicketStatus.PAID)
            );
            double occupancy = (double) booked / (double) capacity * 100.0;
            result.add(new TripOccupancy(trip, capacity, (int) booked, occupancy));
        }
        return result;
    }

    /** Количество проданных и забронированных билетов по датам отправления (от today‑N до today). */
    public Map<LocalDate, Long> getDemandByDate(int daysBack) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(daysBack);
        // Забираем все билеты и группируем по дате отправления рейса
        var tickets = ticketRepository.findAll();
        Map<LocalDate, Long> counts = new TreeMap<>();
        tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.BOOKED || t.getStatus() == TicketStatus.PAID)
                .forEach(t -> {
                    LocalDate date = t.getTrip().getDepartureTime().toLocalDate();
                    if (!date.isBefore(start) && !date.isAfter(today)) {
                        counts.merge(date, 1L, Long::sum);
                    }
                });
        // Заполняем пропущенные даты нулями
        for (int i = 0; i <= daysBack; i++) {
            LocalDate d = start.plusDays(i);
            counts.putIfAbsent(d, 0L);
        }
        return counts;
    }

    /** Распределение билетов по статусам. */
    public Map<TicketStatus, Long> getTicketStatusDistribution() {
        Map<TicketStatus, Long> dist = new EnumMap<>(TicketStatus.class);
        var tickets = ticketRepository.findAll();
        for (var t : tickets) {
            dist.merge(t.getStatus(), 1L, Long::sum);
        }
        return dist;
    }

    /** DTO для графика заполняемости. */
    public record TripOccupancy(Trip trip, int capacity, int booked, double occupancy) {
        public String routeLabel() {
            return trip.getRoute().getOrigin().getName() + " — " + trip.getRoute().getDestination().getName();
        }
        public String formattedDeparture() {
            return trip.getDepartureTime().toString();
        }
    }
}
