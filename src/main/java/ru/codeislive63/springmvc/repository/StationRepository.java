package ru.codeislive63.springmvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.codeislive63.springmvc.domain.entity.Station;

import java.util.Optional;

public interface StationRepository extends JpaRepository<Station, Long> {
    Optional<Station> findByCode(String code);
}
