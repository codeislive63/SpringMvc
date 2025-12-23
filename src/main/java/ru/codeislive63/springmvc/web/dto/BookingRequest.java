package ru.codeislive63.springmvc.web.dto;

import lombok.Data;

/**
 * Данные формы бронирования и выбора дополнительных услуг.
 */
@Data
public class BookingRequest {
    private Long tripId;
    private Long tripId1;
    private Long tripId2;

    private Integer seat;
    private Integer seat1;
    private Integer seat2;

    private String passengerName;
    private String passengerDocument;
    private String benefitType;
    private boolean childTicket;
    private String loyaltyNumber;

    private String mealOption;
    private boolean insuranceIncluded;
    private boolean transferIncluded;
    private boolean baggageSelected;
    private boolean petTravel;
}
