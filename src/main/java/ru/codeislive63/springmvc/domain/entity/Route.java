package ru.codeislive63.springmvc.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Station origin;

    @ManyToOne(optional = false)
    private Station destination;

    @Column(nullable = false)
    private int distanceKm;

    @Column(nullable = false)
    private String name;

    @ElementCollection
    @CollectionTable(name = "route_stops", joinColumns = @JoinColumn(name = "route_id"))
    @Column(name = "stop_name")
    private List<String> stops = new ArrayList<>();
}
