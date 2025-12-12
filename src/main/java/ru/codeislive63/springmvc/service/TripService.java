package ru.codeislive63.springmvc.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.codeislive63.springmvc.domain.entity.Route;
import ru.codeislive63.springmvc.domain.entity.Trip;
import ru.codeislive63.springmvc.repository.RouteRepository;
import ru.codeislive63.springmvc.repository.TripRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;

    public Trip getTrip(Long id) {
        return tripRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Рейс не найден"));
    }

    public List<Trip> searchTrips(Long routeId, LocalDate date) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Маршрут не найден"));
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return tripRepository.findByRouteAndDepartureTimeBetween(route, start, end);
    }

    @Transactional
    public Trip save(Trip trip) {
        return tripRepository.save(trip);
    }
}


