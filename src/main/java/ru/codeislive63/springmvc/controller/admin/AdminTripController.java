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
import ru.codeislive63.springmvc.domain.entity.Trip;
import ru.codeislive63.springmvc.repository.RouteRepository;
import ru.codeislive63.springmvc.repository.TicketRepository;
import ru.codeislive63.springmvc.repository.TrainRepository;
import ru.codeislive63.springmvc.repository.TripRepository;
import ru.codeislive63.springmvc.service.AdminService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("")
    public String listTrips(@RequestParam(value = "q", required = false) String query,
                            Model model) {
        List<Trip> trips = tripRepository.findAll();

        if (query != null && !query.isBlank()) {
            String lower = query.toLowerCase();
            trips = trips.stream()
                    .filter(t ->
                            t.getRoute().getOrigin().getName().toLowerCase().contains(lower)
                                    || t.getRoute().getDestination().getName().toLowerCase().contains(lower)
                                    || t.getTrain().getName().toLowerCase().contains(lower)
                                    || t.getTrain().getCode().toLowerCase().contains(lower)
                    )
                    .collect(Collectors.toList());
        }
        model.addAttribute("trips", trips);
        model.addAttribute("q", query);
        return "pages/admin/trips/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        List<Route> routes = routeRepository.findAll();
        List<Train> trains = trainRepository.findAll();
        model.addAttribute("routes", routes);
        model.addAttribute("trains", trains);
        model.addAttribute("trip", new ru.codeislive63.springmvc.domain.entity.Trip());
        return "pages/admin/trips/form";
    }

    @PostMapping("/create")
    public String create(@RequestParam Long routeId,
                         @RequestParam Long trainId,
                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
                         LocalDateTime departure,
                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
                         LocalDateTime arrival,
                         @RequestParam BigDecimal basePrice,
                         RedirectAttributes ra) {
        try {
            adminService.createTrip(routeId, trainId, departure, arrival, basePrice);
            ra.addFlashAttribute("success", "Рейс создан");
            return "redirect:/admin/panel/trips";
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/panel/trips/new";
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            ra.addFlashAttribute("error", "Нельзя создать рейс: проверьте корректность данных и ограничения БД.");
            return "redirect:/admin/panel/trips/new";
        }
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

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes ra) {
        var trip = tripRepository.findById(id).orElse(null);
        if (trip == null) {
            ra.addFlashAttribute("error", "Рейс не найден");
            return "redirect:/admin/panel/trips";
        }
        model.addAttribute("trip", trip);
        model.addAttribute("routes", routeRepository.findAll());
        model.addAttribute("trains", trainRepository.findAll());
        return "pages/admin/trips/form";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id,
                         @RequestParam Long routeId,
                         @RequestParam Long trainId,
                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime departure,
                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime arrival,
                         @RequestParam BigDecimal basePrice,
                         RedirectAttributes ra) {
        var trip = tripRepository.findById(id).orElse(null);
        if (trip == null) {
            ra.addFlashAttribute("error", "Рейс не найден");
            return "redirect:/admin/panel/trips";
        }

        trip.setRoute(routeRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Маршрут не найден")));
        trip.setTrain(trainRepository.findById(trainId)
                .orElseThrow(() -> new IllegalArgumentException("Поезд не найден")));
        trip.setDepartureTime(departure);
        trip.setArrivalTime(arrival);
        trip.setBasePrice(basePrice);
        tripRepository.save(trip);
        ra.addFlashAttribute("success", "Рейс обновлён");
        return "redirect:/admin/panel/trips";
    }
}
