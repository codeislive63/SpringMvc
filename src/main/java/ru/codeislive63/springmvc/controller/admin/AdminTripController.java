package ru.codeislive63.springmvc.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.codeislive63.springmvc.domain.TicketStatus;
import ru.codeislive63.springmvc.domain.entity.Route;
import ru.codeislive63.springmvc.domain.entity.Train;
import ru.codeislive63.springmvc.repository.RouteRepository;
import ru.codeislive63.springmvc.repository.TicketRepository;
import ru.codeislive63.springmvc.repository.TrainRepository;
import ru.codeislive63.springmvc.repository.TripRepository;
import ru.codeislive63.springmvc.service.AdminService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/panel/trips")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminTripController {

    private final TripRepository tripRepository;
    private final TicketRepository ticketRepository;
    private final RouteRepository routeRepository;
    private final TrainRepository trainRepository;
    private final AdminService adminService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("trips", tripRepository.findAll());
        return "admin/trips/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        List<Route> routes = routeRepository.findAll();
        List<Train> trains = trainRepository.findAll();
        model.addAttribute("routes", routes);
        model.addAttribute("trains", trains);
        return "admin/trips/form";
    }

    @PostMapping("/create")
    public String create(@RequestParam Long routeId,
                         @RequestParam Long trainId,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                         LocalDateTime departure,
                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                         LocalDateTime arrival,
                         @RequestParam BigDecimal basePrice) {

        adminService.createTrip(routeId, trainId, departure, arrival, basePrice);
        return "redirect:/admin/panel/trips";
    }

    @PostMapping("/{id}/delete")
    @Transactional
    public String delete(@PathVariable Long id, RedirectAttributes ra) {

        long activeTickets = ticketRepository.countByTripIdAndStatusIn(
                id,
                List.of(TicketStatus.BOOKED, TicketStatus.PAID)
        );

        if (activeTickets > 0) {
            ra.addFlashAttribute("error", "Нельзя удалить рейс: есть активные билеты (" + activeTickets + ")");
            return "redirect:/admin/panel/trips";
        }

        ticketRepository.deleteByTripIdAndStatusIn(
                id,
                List.of(TicketStatus.CANCELLED, TicketStatus.REFUNDED)
        );

        tripRepository.deleteById(id);

        ra.addFlashAttribute("success", "Рейс удалён");
        return "redirect:/admin/panel/trips";
    }
}
