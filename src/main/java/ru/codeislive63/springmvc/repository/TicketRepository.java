package ru.codeislive63.springmvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.codeislive63.springmvc.domain.TicketStatus;
import ru.codeislive63.springmvc.domain.entity.Ticket;
import ru.codeislive63.springmvc.domain.entity.Trip;
import ru.codeislive63.springmvc.domain.entity.UserAccount;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByUser(UserAccount user);
    Optional<Ticket> findByIdAndUser(Long id, UserAccount user);
    void deleteByTripIdAndStatusIn(Long tripId, java.util.Collection<ru.codeislive63.springmvc.domain.TicketStatus> statuses);
    long countByTripIdAndStatusIn(Long tripId, Collection<TicketStatus> statuses);
}
