package ru.codeislive63.springmvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.codeislive63.springmvc.repository.RouteRepository;
import ru.codeislive63.springmvc.security.UserPrincipal;
import ru.codeislive63.springmvc.service.TripService;
import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final TripService tripService;
    private final RouteRepository routeRepository;

    @GetMapping("/")
    public String index(@AuthenticationPrincipal UserPrincipal principal, Model model) {
        if (principal != null) {
            return "redirect:/dashboard";
        }

        model.addAttribute("routes", routeRepository.findAll());
        return "pages/home/index";
    }

    @GetMapping("/trips/search")
    public String searchPage(Model model) {
        model.addAttribute("routes", routeRepository.findAll());
        return "pages/home/index";
    }

    @GetMapping("/search")
    public String search(
            @RequestParam(value = "routeId", required = false) Long routeId,
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (routeId == null) {
            redirectAttributes.addFlashAttribute("error", "Пожалуйста, выберите маршрут");
            return "redirect:/";
        }

        model.addAttribute("trips", tripService.searchTrips(routeId, date));
        return "pages/trips/list";
    }
}
