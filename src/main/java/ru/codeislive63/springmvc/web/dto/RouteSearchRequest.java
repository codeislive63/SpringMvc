package ru.codeislive63.springmvc.web.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class RouteSearchRequest {

    @NotNull
    private Long fromPointId;

    @NotNull
    private Long toPointId;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate departureDate;
}
