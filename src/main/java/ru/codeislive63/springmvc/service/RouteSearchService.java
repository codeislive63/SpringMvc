package ru.codeislive63.springmvc.service;

import lombok.RequiredArgsConstructor;
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

    private static final Duration MIN_TRANSFER = Duration.ofMinutes(20);
    private static final Duration MAX_TRANSFER = Duration.ofHours(6);

    public List<ItineraryDto> search(RouteSearchRequest req) {
        LocalDateTime fromDt = req.getDepartureDate().atStartOfDay();
        LocalDateTime toDt = req.getDepartureDate().atTime(LocalTime.MAX);

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

            LocalDateTime minDep = first.getArrivalTime().plus(MIN_TRANSFER);
            LocalDateTime maxDep = first.getArrivalTime().plus(MAX_TRANSFER);

            if (maxDep.isBefore(fromDt) || minDep.isAfter(toDt)) {
                continue;
            }

            LocalDateTime realMin = minDep.isBefore(fromDt) ? fromDt : minDep;
            LocalDateTime realMax = maxDep.isAfter(toDt) ? toDt : maxDep;

            List<Trip> seconds = tripRepository.findSecondLegs(transferId, destinationId, realMin, realMax);

            for (Trip second : seconds) {
                Long secondDestId = second.getRoute().getDestination().getId();

                if (Objects.equals(secondDestId, originId)) {
                    continue;
                }

                withTransfer.add(itineraryOf(List.of(toLeg(first), toLeg(second))));
            }
        }

        return mergeAndSort(direct, withTransfer);
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

        return TripLegDto.builder()
                .tripId(t.getId())
                .routeId(r.getId())
                .fromName(r.getOrigin().getName())
                .toName(r.getDestination().getName())
                .departureTime(t.getDepartureTime())
                .arrivalTime(t.getArrivalTime())
                .price(t.getBasePrice())
                .trainName(t.getTrain() != null ? t.getTrain().getName() : null)
                .build();
    }

    private String formatDuration(Duration d) {
        long hours = d.toHours();
        long minutes = d.minusHours(hours).toMinutes();

        if (hours == 0) return minutes + " мин";
        if (minutes == 0) return hours + " ч";
        return hours + " ч " + minutes + " мин";
    }
}
