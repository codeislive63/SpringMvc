package ru.codeislive63.springmvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.codeislive63.springmvc.domain.entity.Payment;
import ru.codeislive63.springmvc.domain.entity.Ticket;
import ru.codeislive63.springmvc.security.UserPrincipal;
import ru.codeislive63.springmvc.service.BookingService;

import java.util.List;

@Controller
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/{tripId}")
    public ResponseEntity<Ticket> book(@PathVariable Long tripId,
                                       @AuthenticationPrincipal UserPrincipal principal) {
        Ticket ticket = bookingService.bookTicket(principal.user().getId(), tripId);
        return ResponseEntity.ok(ticket);
    }

    @PostMapping("/pay/{ticketId}")
    public ResponseEntity<Payment> pay(@PathVariable Long ticketId) {
        return ResponseEntity.ok(bookingService.payTicket(ticketId));
    }

    @PostMapping("/cancel/{ticketId}")
    public ResponseEntity<Ticket> cancel(@PathVariable Long ticketId,
                                         @AuthenticationPrincipal UserPrincipal principal) {
        Ticket ticket = bookingService.cancel(ticketId, principal.user().getId());
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/my")
    public ResponseEntity<List<Ticket>> my(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(bookingService.myTickets(principal.user().getId()));
    }

    @GetMapping("/start")
    public String start(@RequestParam(required = false) Long tripId,
                        @RequestParam(required = false) Long tripId1,
                        @RequestParam(required = false) Long tripId2,
                        Model model) {
        // If single trip selected, prepare available seats
        if (tripId != null) {
            model.addAttribute("tripId", tripId);
            model.addAttribute("seats", bookingService.availableSeats(tripId));
        }
        // If two trips selected (transfer), prepare seats for both legs
        if (tripId1 != null && tripId2 != null) {
            model.addAttribute("tripId1", tripId1);
            model.addAttribute("tripId2", tripId2);
            model.addAttribute("seats1", bookingService.availableSeats(tripId1));
            model.addAttribute("seats2", bookingService.availableSeats(tripId2));
        }
        return "booking/select-seat";
    }

    /**
     * Handles reservation requests when the user has chosen specific seats.
     * If a single trip is selected, expects parameters {@code tripId} and {@code seat}.
     * If a transfer route is selected, expects {@code tripId1}, {@code tripId2}, {@code seat1} and {@code seat2}.
     */
    @PostMapping("/reserve")
    public String reserve(@RequestParam(required = false) Long tripId,
                          @RequestParam(required = false) Long tripId1,
                          @RequestParam(required = false) Long tripId2,
                          @RequestParam(required = false) Integer seat,
                          @RequestParam(required = false) Integer seat1,
                          @RequestParam(required = false) Integer seat2,
                          @AuthenticationPrincipal UserPrincipal principal,
                          Model model) {
        if (tripId != null && seat != null) {
            Ticket ticket = bookingService.bookSeat(principal.user().getId(), tripId, seat);
            model.addAttribute("tickets", List.of(ticket));
        } else if (tripId1 != null && tripId2 != null && seat1 != null && seat2 != null) {
            Ticket ticketA = bookingService.bookSeat(principal.user().getId(), tripId1, seat1);
            Ticket ticketB = bookingService.bookSeat(principal.user().getId(), tripId2, seat2);
            model.addAttribute("tickets", List.of(ticketA, ticketB));
        } else {
            throw new IllegalArgumentException("Неверные параметры для бронирования");
        }
        return "booking/confirmation";
    }
}
