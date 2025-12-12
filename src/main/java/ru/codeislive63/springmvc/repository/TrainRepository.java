package ru.codeislive63.springmvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.codeislive63.springmvc.domain.entity.Train;

import java.util.Optional;

public interface TrainRepository extends JpaRepository<Train, Long> {
    Optional<Train> findByCode(String code);
}
