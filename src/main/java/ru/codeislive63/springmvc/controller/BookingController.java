package ru.codeislive63.springmvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.codeislive63.springmvc.domain.entity.Payment;
import ru.codeislive63.springmvc.domain.entity.Ticket;
import ru.codeislive63.springmvc.domain.entity.Trip;
import ru.codeislive63.springmvc.security.UserPrincipal;
import ru.codeislive63.springmvc.service.BookingService;
import ru.codeislive63.springmvc.service.TripService;
import ru.codeislive63.springmvc.web.dto.BookingRequest;

import java.util.List;

@Controller
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final TripService tripService;

    @PostMapping("/{tripId}")
    public ResponseEntity<Ticket> book(@PathVariable Long tripId,
                                       @AuthenticationPrincipal UserPrincipal principal) {
        Ticket ticket = bookingService.bookTicket(principal.user().getId(), tripId);
        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/pay/{ticketId}")
    public String pay(@PathVariable Long ticketId,
                      @AuthenticationPrincipal UserPrincipal principal,
                      RedirectAttributes ra) {
        try {
            bookingService.payTicket(ticketId); // лучше: payTicket(ticketId, principal.user().getId())
            ra.addFlashAttribute("success", "Оплата прошла успешно ✅");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile";
    }
    @PostMapping("/cancel/{ticketId}")
    public ResponseEntity<Ticket> cancel(@PathVariable Long ticketId,
                                         @AuthenticationPrincipal UserPrincipal principal) {
        Ticket ticket = bookingService.cancel(ticketId, principal.user().getId());
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/my")
    public String my(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        List<Ticket> tickets = bookingService.myTickets(principal.user().getId());
        model.addAttribute("tickets", tickets);
        return "pages/tickets/list";
    }

    @GetMapping("/start")
    public String start(@RequestParam(required = false) Long tripId,
                        @RequestParam(required = false) Long tripId1,
                        @RequestParam(required = false) Long tripId2,
                        Model model) {

        if (tripId != null) {
            Trip trip = tripService.getTrip(tripId);
            model.addAttribute("tripId", tripId);
            model.addAttribute("trip", trip);
            model.addAttribute("routeLabel", routeLabel(trip));
            model.addAttribute("seats", bookingService.availableSeats(tripId));
            model.addAttribute("seatMap", bookingService.seatMap(tripId));
        }

        if (tripId1 != null && tripId2 != null) {
            Trip tripA = tripService.getTrip(tripId1);
            Trip tripB = tripService.getTrip(tripId2);
            model.addAttribute("tripId1", tripId1);
            model.addAttribute("tripId2", tripId2);
            model.addAttribute("tripA", tripA);
            model.addAttribute("tripB", tripB);
            model.addAttribute("routeLabelA", routeLabel(tripA));
            model.addAttribute("routeLabelB", routeLabel(tripB));
            model.addAttribute("seats1", bookingService.availableSeats(tripId1));
            model.addAttribute("seats2", bookingService.availableSeats(tripId2));
            model.addAttribute("seatMap1", bookingService.seatMap(tripId1));
            model.addAttribute("seatMap2", bookingService.seatMap(tripId2));
        }
        return "pages/booking/select-seat";
    }

    @PostMapping("/reserve")
    public String reserve(@ModelAttribute BookingRequest request,
                          @AuthenticationPrincipal UserPrincipal principal,
                          Model model) {
        if (request.getTripId() != null && request.getSeat() != null) {
            Ticket ticket = bookingService.bookSeat(
                    principal.user().getId(),
                    request.getTripId(),
                    request.getSeat(),
                    request);
            model.addAttribute("tickets", List.of(ticket));
        } else if (request.getTripId1() != null && request.getTripId2() != null
                && request.getSeat1() != null && request.getSeat2() != null) {

            Ticket ticketA = bookingService.bookSeat(
                    principal.user().getId(),
                    request.getTripId1(),
                    request.getSeat1(),
                    request);

            Ticket ticketB = bookingService.bookSeat(
                    principal.user().getId(),
                    request.getTripId2(),
                    request.getSeat2(),
                    request);

            model.addAttribute("tickets", List.of(ticketA, ticketB));
        } else {
            throw new IllegalArgumentException("Неверные параметры для бронирования");
        }
        model.addAttribute("passenger", request);
        return "pages/booking/confirmation";
    }

    private String routeLabel(Trip trip) {
        if (trip == null || trip.getRoute() == null) {
            return "Маршрут";
        }
        var r = trip.getRoute();
        var from = (r.getOrigin() != null && r.getOrigin().getName() != null) ? r.getOrigin().getName() : "";
        var to = (r.getDestination() != null && r.getDestination().getName() != null) ? r.getDestination().getName() : "";
        return String.format("%s — %s", from, to);
    }
}
