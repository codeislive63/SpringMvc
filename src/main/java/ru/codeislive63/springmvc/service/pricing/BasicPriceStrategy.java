package ru.codeislive63.springmvc.service.pricing;

import org.springframework.stereotype.Component;
import ru.codeislive63.springmvc.domain.entity.Trip;

import java.math.BigDecimal;

@Component("basicPriceStrategy")
public class BasicPriceStrategy implements PriceStrategy {
    @Override
    public BigDecimal calculate(Trip trip, int passengers) {
        return trip.getBasePrice().multiply(BigDecimal.valueOf(passengers));
    }
}


