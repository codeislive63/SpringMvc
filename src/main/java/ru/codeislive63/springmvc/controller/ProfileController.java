package ru.codeislive63.springmvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.util.StringUtils;
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

        if (principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard";
        }

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
        model.addAttribute("savedPassengers", extractPassengers(tickets, user.getFullName()));
        model.addAttribute("savedDocuments", extractDocuments(tickets));
        model.addAttribute("loyaltyPoints", paid.size() * 120 + unpaid.size() * 40);
        model.addAttribute("loyaltyTier", determineTier(paid.size()));
        return "pages/profile/index";
    }

    private List<String> extractPassengers(List<Ticket> tickets, String fallback) {
        List<String> names = tickets.stream()
                .map(Ticket::getPassengerDetails)
                .filter(details -> details != null && StringUtils.hasText(details.getFullName()))
                .map(details -> details.getFullName().trim())
                .distinct()
                .toList();

        if (names.isEmpty() && StringUtils.hasText(fallback)) {
            return List.of(fallback);
        }
        return names;
    }

    private List<String> extractDocuments(List<Ticket> tickets) {
        return tickets.stream()
                .map(Ticket::getPassengerDetails)
                .filter(details -> details != null && StringUtils.hasText(details.getDocumentNumber()))
                .map(details -> details.getDocumentNumber().trim())
                .distinct()
                .toList();
    }

    private String determineTier(int paidTickets) {
        if (paidTickets > 6) return "Платиновый";
        if (paidTickets > 3) return "Золотой";
        if (paidTickets > 0) return "Серебряный";
        return "Базовый";
    }
}
