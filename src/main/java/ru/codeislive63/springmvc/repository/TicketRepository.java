package ru.codeislive63.springmvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Returns seat numbers that are currently booked (including paid) for the given trip.
     * Only tickets with statuses in the provided collection are considered; typically {@code BOOKED} and {@code PAID}.
     *
     * @param tripId   ID of the trip
     * @param statuses statuses to include when looking for occupied seats
     * @return list of seat numbers that are not free
     */
    @Query("select t.seatNumber from Ticket t where t.trip.id = :tripId and t.status in :statuses")
    List<Integer> findBookedSeats(@Param("tripId") Long tripId, @Param("statuses") Collection<TicketStatus> statuses);
}
