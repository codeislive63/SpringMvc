package ru.codeislive63.springmvc.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.codeislive63.springmvc.domain.entity.Route;
import ru.codeislive63.springmvc.domain.entity.Station;
import ru.codeislive63.springmvc.domain.entity.Train;
import ru.codeislive63.springmvc.domain.entity.Trip;
import ru.codeislive63.springmvc.repository.RouteRepository;
import ru.codeislive63.springmvc.repository.StationRepository;
import ru.codeislive63.springmvc.repository.TrainRepository;
import ru.codeislive63.springmvc.repository.TripRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final StationRepository stationRepository;
    private final RouteRepository routeRepository;
    private final TrainRepository trainRepository;
    private final TripRepository tripRepository;

    public Station createStation(String code, String name) {
        return stationRepository.findByCode(code).orElseGet(() -> {
            Station station = new Station();
            station.setCode(code);
            station.setName(name);
            return stationRepository.save(station);
        });
    }

    public Train createTrain(String code, String name, int capacity) {
        return trainRepository.findByCode(code).orElseGet(() -> {
            Train train = new Train();
            train.setCode(code);
            train.setName(name);
            train.setSeatCapacity(capacity);
            return trainRepository.save(train);
        });
    }

    @Transactional
    public Route createRoute(Long originId, Long destinationId, int distanceKm, String name) {
        Station origin = stationRepository.findById(originId)
                .orElseThrow(() -> new IllegalArgumentException("Станция отправления не найдена"));
        Station destination = stationRepository.findById(destinationId)
                .orElseThrow(() -> new IllegalArgumentException("Станция прибытия не найдена"));
        return routeRepository.findByOriginAndDestination(origin, destination).orElseGet(() -> {
            Route route = new Route();
            route.setOrigin(origin);
            route.setDestination(destination);
            route.setDistanceKm(distanceKm);
            route.setName(name);
            return routeRepository.save(route);
        });
    }

    @Transactional
    public Trip createTrip(Long routeId, Long trainId, LocalDateTime departure, LocalDateTime arrival, BigDecimal basePrice) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Маршрут не найден"));
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new IllegalArgumentException("Поезд не найден"));
        Trip trip = new Trip();
        trip.setRoute(route);
        trip.setTrain(train);
        trip.setDepartureTime(departure);
        trip.setArrivalTime(arrival);
        trip.setBasePrice(basePrice);
        trip.setSeatsAvailable(train.getSeatCapacity());
        return tripRepository.save(trip);
    }
}

