package ru.codeislive63.springmvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
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
}
