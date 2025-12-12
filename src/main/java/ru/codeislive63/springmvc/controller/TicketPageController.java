package ru.codeislive63.springmvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.codeislive63.springmvc.security.UserPrincipal;
import ru.codeislive63.springmvc.service.BookingService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/tickets")
public class TicketPageController {

    private final BookingService bookingService;

    @GetMapping
    public String myTickets(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        model.addAttribute("tickets", bookingService.myTickets(principal.user().getId()));
        return "tickets/list";
    }

    @PostMapping("/{ticketId}/refund")
    public String refund(@PathVariable Long ticketId,
                         @AuthenticationPrincipal UserPrincipal principal,
                         RedirectAttributes ra) {
        try {
            bookingService.cancel(ticketId, principal.user().getId());
            ra.addFlashAttribute("success", "Билет возвращён");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tickets";
    }
}
