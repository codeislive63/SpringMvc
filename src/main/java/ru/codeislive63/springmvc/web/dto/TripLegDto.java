package ru.codeislive63.springmvc.web.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
}
