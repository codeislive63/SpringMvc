package ru.codeislive63.springmvc.web.dto;

import lombok.Value;

@Value
public class SeatView {
    int seatNumber;
    boolean available;
}
