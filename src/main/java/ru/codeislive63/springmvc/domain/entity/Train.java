package ru.codeislive63.springmvc.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.codeislive63.springmvc.domain.CarClass;
import ru.codeislive63.springmvc.domain.TrainType;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "trains")
public class Train {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrainType type = TrainType.EXPRESS;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CarClass carClass = CarClass.ECONOMY;

    @Column(nullable = false)
    private boolean wifiAvailable = false;

    @Column(nullable = false)
    private boolean diningAvailable = false;

    @Column(nullable = false)
    private boolean powerOutlets = false;

    @Column(nullable = false)
    private int seatCapacity;
}
