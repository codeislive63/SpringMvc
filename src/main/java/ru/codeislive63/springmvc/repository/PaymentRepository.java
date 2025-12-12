package ru.codeislive63.springmvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.codeislive63.springmvc.domain.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
