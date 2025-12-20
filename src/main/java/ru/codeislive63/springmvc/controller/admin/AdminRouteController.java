package ru.codeislive63.springmvc.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.codeislive63.springmvc.domain.entity.Route;
import ru.codeislive63.springmvc.domain.entity.Station;
import ru.codeislive63.springmvc.repository.RouteRepository;
import ru.codeislive63.springmvc.repository.StationRepository;
import ru.codeislive63.springmvc.repository.TripRepository;

@Controller
@RequestMapping("/admin/panel/routes")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminRouteController {

    private final RouteRepository routeRepository;
    private final StationRepository stationRepository;
    private final TripRepository tripRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("routes", routeRepository.findAll());
        return "admin/routes/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("route", new Route());
        model.addAttribute("stations", stationRepository.findAll());
        return "admin/routes/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Route route = routeRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Маршрут не найден"));

        model.addAttribute("route", route);
        model.addAttribute("stations", stationRepository.findAll());
        return "admin/routes/form";
    }

    @PostMapping("/create")
    public String create(@RequestParam(required = false) Long id,
                       @RequestParam Long originId,
                       @RequestParam Long destinationId,
                       @RequestParam(required = false) String name,
                       RedirectAttributes ra) {

        if (originId.equals(destinationId)) {
            ra.addFlashAttribute("error", "Станции отправления и назначения должны отличаться");
            return (id == null)
                    ? "redirect:/admin/panel/routes/new"
                    : "redirect:/admin/panel/routes/" + id + "/edit";
        }

        Station origin = stationRepository
                .findById(originId)
                .orElseThrow(() -> new IllegalArgumentException("Станция не найдена"));

        Station destination = stationRepository
                .findById(destinationId)
                .orElseThrow(() -> new IllegalArgumentException("Станция не найдена"));

        Route route = (id == null)
                ? new Route()
                : routeRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Маршрут не найден"));

        route.setOrigin(origin);
        route.setDestination(destination);

        String routeName = (name == null || name.isBlank())
                ? origin.getName() + " — " + destination.getName()
                : name.trim();

        route.setName(routeName);

        routeRepository.save(route);
        ra.addFlashAttribute("success", "Маршрут сохранён");
        return "redirect:/admin/panel/routes";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {

        long trips = tripRepository.countByRouteId(id);

        if (trips > 0) {
            ra.addFlashAttribute("error", "Нельзя удалить маршрут: есть рейсы (" + trips + ")");
            return "redirect:/admin/panel/routes";
        }

        routeRepository.deleteById(id);
        ra.addFlashAttribute("success", "Маршрут удалён");
        return "redirect:/admin/panel/routes";
    }
}
