package ru.codeislive63.springmvc.service.pricing;

import ru.codeislive63.springmvc.domain.entity.Trip;

import java.math.BigDecimal;

public interface PriceStrategy {
    BigDecimal calculate(Trip trip, int passengers);
}


