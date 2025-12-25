package ru.codeislive63.springmvc.web.dto;

import lombok.Builder;
import lombok.Value;
import ru.codeislive63.springmvc.domain.TrainClass;
import ru.codeislive63.springmvc.domain.TrainType;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class TripLegDto {
    Long tripId;
    Long routeId;
    String fromName;
    String toName;
    LocalDateTime departureTime;
    LocalDateTime arrivalTime;
    BigDecimal price;
    String trainName;
    TrainType trainType;
    TrainClass trainClass;
    boolean wifiAvailable;
    boolean diningAvailable;
    boolean powerOutlets;
    List<String> stops;
    Duration legDuration;
    int seatsAvailable;
}
