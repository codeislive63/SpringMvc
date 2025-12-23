package ru.codeislive63.springmvc.web.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class ItineraryDto {
    List<TripLegDto> legs;

    BigDecimal totalPrice;
    Duration totalDuration;
    String totalDurationText;

    public int transfers() {
        return Math.max(0, legs.size() - 1);
    }

    public LocalDateTime departure() {
        return legs.getFirst().getDepartureTime();
    }

    public LocalDateTime arrival() {
        return legs.getLast().getArrivalTime();
    }
}
