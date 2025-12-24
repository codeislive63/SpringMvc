package ru.codeislive63.springmvc.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import ru.codeislive63.springmvc.domain.CarClass;
import ru.codeislive63.springmvc.domain.TrainType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class RouteSearchRequest {

    @NotNull
    private Long fromPointId;

    @NotNull
    private Long toPointId;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate departureDate;

    // Поля для автокомплита (чтобы вернуть введённый текст на форму)
    private String fromPointName;
    private String toPointName;

    private TrainType trainType;
    private CarClass carClass;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime departureFrom;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime arrivalTo;

    private BigDecimal maxPrice;
}
