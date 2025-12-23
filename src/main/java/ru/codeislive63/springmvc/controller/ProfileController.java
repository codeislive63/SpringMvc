package ru.codeislive63.springmvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.codeislive63.springmvc.domain.TicketStatus;
import ru.codeislive63.springmvc.domain.entity.Ticket;
import ru.codeislive63.springmvc.security.UserPrincipal;
import ru.codeislive63.springmvc.service.BookingService;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final BookingService bookingService;

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        var user = principal.user();
        List<Ticket> tickets = bookingService.myTickets(user.getId());

        List<Ticket> unpaid = tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.BOOKED)
                .collect(Collectors.toList());
        List<Ticket> paid = tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.PAID)
                .collect(Collectors.toList());
        List<Ticket> cancelled = tickets.stream()
                .filter(t -> t.getStatus() == TicketStatus.CANCELLED || t.getStatus() == TicketStatus.REFUNDED)
                .collect(Collectors.toList());

        model.addAttribute("user", user);
        model.addAttribute("unpaidTickets", unpaid);
        model.addAttribute("paidTickets", paid);
        model.addAttribute("cancelledTickets", cancelled);
        return "profile";
    }
}
