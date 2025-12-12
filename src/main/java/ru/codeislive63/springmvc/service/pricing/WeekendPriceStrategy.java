package ru.codeislive63.springmvc.service.pricing;

import org.springframework.stereotype.Component;
import ru.codeislive63.springmvc.domain.entity.Trip;

import java.math.BigDecimal;
import java.time.DayOfWeek;

@Component("weekendPriceStrategy")
public class WeekendPriceStrategy implements PriceStrategy {
    @Override
    public BigDecimal calculate(Trip trip, int passengers) {
        boolean weekend = trip.getDepartureTime().getDayOfWeek() == DayOfWeek.SATURDAY
                || trip.getDepartureTime().getDayOfWeek() == DayOfWeek.SUNDAY;
        BigDecimal base = trip.getBasePrice().multiply(BigDecimal.valueOf(passengers));
        return weekend ? base.multiply(BigDecimal.valueOf(1.15)) : base;
    }
}


