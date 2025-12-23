package ru.codeislive63.springmvc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.codeislive63.springmvc.domain.entity.Route;
import ru.codeislive63.springmvc.domain.entity.Trip;
import ru.codeislive63.springmvc.repository.TripRepository;
import ru.codeislive63.springmvc.web.dto.ItineraryDto;
import ru.codeislive63.springmvc.web.dto.RouteSearchRequest;
import ru.codeislive63.springmvc.web.dto.TripLegDto;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteSearchService {

    private final TripRepository tripRepository;

    @Value("${route.min-transfer:20}")
    private int minTransferMinutes;

    @Value("${route.max-transfer-hours:6}")
    private int maxTransferHours;

    public List<ItineraryDto> search(RouteSearchRequest req) {
        Duration minTransfer = Duration.ofMinutes(minTransferMinutes);
        Duration maxTransfer = Duration.ofHours(maxTransferHours);

        LocalDateTime fromDt = req.getDepartureDate().atStartOfDay();
        LocalDateTime toDt = req.getDepartureDate().atTime(LocalTime.MAX);

        LocalDateTime searchEnd = toDt.plusDays(1);

        Long originId = req.getFromPointId();
        Long destinationId = req.getToPointId();

        List<ItineraryDto> direct = tripRepository.findDirectTrips(originId, destinationId, fromDt, toDt)
                .stream()
                .map(t -> itineraryOf(List.of(toLeg(t))))
                .toList();

        List<Trip> firstLegs = tripRepository.findFirstLegs(originId, fromDt, toDt);

        List<ItineraryDto> withTransfer = new ArrayList<>();

        for (Trip first : firstLegs) {
            Route firstRoute = first.getRoute();

            Long transferId = firstRoute.getDestination().getId();

            if (Objects.equals(transferId, originId)) {
                continue;
            }

            if (Objects.equals(transferId, destinationId)){
                continue;
            }

            LocalDateTime minDep = first.getArrivalTime().plus(minTransfer);
            LocalDateTime maxDep = first.getArrivalTime().plus(maxTransfer);

            if (maxDep.isBefore(fromDt)) {
                continue;
            }

            LocalDateTime realMin = minDep.isBefore(fromDt) ? fromDt : minDep;
            List<Trip> seconds = tripRepository.findSecondLegs(transferId, destinationId, realMin, searchEnd);

            for (Trip second : seconds) {
                Long secondDestId = second.getRoute().getDestination().getId();

                if (Objects.equals(secondDestId, originId)) {
                    continue;
                }

                withTransfer.add(itineraryOf(List.of(toLeg(first), toLeg(second))));
            }
        }

        List<ItineraryDto> merged = mergeAndSort(direct, withTransfer);
        return applyFilters(merged, req);
    }

    private List<ItineraryDto> mergeAndSort(List<ItineraryDto> direct, List<ItineraryDto> transfer) {
        Map<String, ItineraryDto> uniq = new LinkedHashMap<>();

        for (ItineraryDto it : concat(direct, transfer)) {
            String key = it.getLegs().stream()
                    .map(l -> String.valueOf(l.getTripId()))
                    .collect(Collectors.joining("-"));

            uniq.putIfAbsent(key, it);
        }

        return uniq.values().stream()
                .sorted(Comparator
                        .comparing(ItineraryDto::departure)
                        .thenComparing(ItineraryDto::getTotalPrice))
                .toList();
    }

    private List<ItineraryDto> concat(List<ItineraryDto> a, List<ItineraryDto> b) {
        List<ItineraryDto> out = new ArrayList<>(a.size() + b.size());
        out.addAll(a);
        out.addAll(b);
        return out;
    }

    private ItineraryDto itineraryOf(List<TripLegDto> legs) {
        BigDecimal total = legs.stream()
                .map(TripLegDto::getPrice)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Duration dur = Duration.between(
                legs.getFirst().getDepartureTime(),
                legs.getLast().getArrivalTime()
        );

        return ItineraryDto.builder()
                .legs(legs)
                .totalPrice(total)
                .totalDuration(dur)
                .totalDurationText(formatDuration(dur))
                .build();
    }

    private TripLegDto toLeg(Trip t) {
        Route r = t.getRoute();
        var train = t.getTrain();
        var duration = Duration.between(t.getDepartureTime(), t.getArrivalTime());

        return TripLegDto.builder()
                .tripId(t.getId())
                .routeId(r.getId())
                .fromName(r.getOrigin().getName())
                .toName(r.getDestination().getName())
                .departureTime(t.getDepartureTime())
                .arrivalTime(t.getArrivalTime())
                .price(t.getBasePrice())
                .trainName(train != null ? train.getName() : null)
                .trainType(train != null ? train.getType() : null)
                .carClass(train != null ? train.getCarClass() : null)
                .wifiAvailable(train != null && train.isWifiAvailable())
                .diningAvailable(train != null && train.isDiningAvailable())
                .powerOutlets(train != null && train.isPowerOutlets())
                .stops(r.getStops())
                .legDuration(duration)
                .seatsAvailable(t.getSeatsAvailable())
                .build();
    }

    private String formatDuration(Duration d) {
        long hours = d.toHours();
        long minutes = d.minusHours(hours).toMinutes();

        if (hours == 0) return minutes + " мин";
        if (minutes == 0) return hours + " ч";
        return hours + " ч " + minutes + " мин";
    }

    private List<ItineraryDto> applyFilters(List<ItineraryDto> itineraries, RouteSearchRequest req) {
        return itineraries.stream()
                .filter(it -> req.getTrainType() == null || it.getLegs().stream()
                        .allMatch(l -> l.getTrainType() == req.getTrainType()))
                .filter(it -> req.getCarClass() == null || it.getLegs().stream()
                        .allMatch(l -> l.getCarClass() == req.getCarClass()))
                .filter(it -> req.getDepartureFrom() == null || !it.departure().toLocalTime()
                        .isBefore(req.getDepartureFrom()))
                .filter(it -> req.getArrivalTo() == null || !it.arrival().toLocalTime()
                        .isAfter(req.getArrivalTo()))
                .filter(it -> req.getMaxPrice() == null || it.getTotalPrice().compareTo(req.getMaxPrice()) <= 0)
                .toList();
    }
}
