package ru.codeislive63.springmvc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.codeislive63.springmvc.domain.entity.Route;
import ru.codeislive63.springmvc.domain.entity.Trip;

import java.time.LocalDateTime;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {
    @Query("""
        select t from Trip t
        where t.route.origin.id = :originId
          and t.route.destination.id = :destinationId
          and t.departureTime between :start and :end
        order by t.departureTime asc
    """)
    List<Trip> findDirectTrips(
            @Param("originId") Long originId,
            @Param("destinationId") Long destinationId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        select t from Trip t
        where t.route.origin.id = :originId
          and t.departureTime between :start and :end
        order by t.departureTime asc
    """)
    List<Trip> findFirstLegs(
            @Param("originId") Long originId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
        select t from Trip t
        where t.route.origin.id = :transferId
          and t.route.destination.id = :destinationId
          and t.departureTime between :minDep and :maxDep
        order by t.departureTime asc
    """)
    List<Trip> findSecondLegs(
            @Param("transferId") Long transferId,
            @Param("destinationId") Long destinationId,
            @Param("minDep") LocalDateTime minDep,
            @Param("maxDep") LocalDateTime maxDep
    );

    List<Trip> findByRouteAndDepartureTimeBetween(Route route, LocalDateTime start, LocalDateTime end);
    long countByRouteId(Long routeId);
}
