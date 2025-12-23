package ru.codeislive63.springmvc.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class PassengerDetails {

    @Column(name = "passenger_name")
    private String fullName;

    @Column(name = "passenger_document")
    private String documentNumber;

    @Column(name = "passenger_benefit")
    private String benefitType;

    @Column(name = "passenger_is_child")
    private boolean childTicket;

    @Column(name = "loyalty_number")
    private String loyaltyNumber;
}
