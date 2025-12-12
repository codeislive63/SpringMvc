package ru.codeislive63.springmvc.service.pricing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.codeislive63.springmvc.domain.entity.Trip;

import java.time.DayOfWeek;

@Component
@RequiredArgsConstructor
public class PriceStrategyFactory {

    private final BasicPriceStrategy basicPriceStrategy;
    private final WeekendPriceStrategy weekendPriceStrategy;

    public PriceStrategy chooseFor(Trip trip) {
        DayOfWeek day = trip.getDepartureTime().getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return weekendPriceStrategy;
        }
        return basicPriceStrategy;
    }
}


