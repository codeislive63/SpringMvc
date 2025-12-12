package ru.codeislive63.springmvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.codeislive63.springmvc.domain.entity.Route;
import ru.codeislive63.springmvc.domain.entity.Station;

import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route, Long> {
    Optional<Route> findByOriginAndDestination(Station origin, Station destination);
}
