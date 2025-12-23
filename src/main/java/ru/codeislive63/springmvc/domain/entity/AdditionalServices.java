package ru.codeislive63.springmvc.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class AdditionalServices {

    @Column(name = "meal_option")
    private String mealOption;

    @Column(name = "insurance_included")
    private boolean insuranceIncluded;

    @Column(name = "transfer_included")
    private boolean transferIncluded;

    @Column(name = "baggage_selected")
    private boolean baggageSelected;

    @Column(name = "pet_travel")
    private boolean petTravel;
}
