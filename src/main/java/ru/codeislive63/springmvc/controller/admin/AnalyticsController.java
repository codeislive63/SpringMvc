package ru.codeislive63.springmvc.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.codeislive63.springmvc.service.AnalyticsService;

@Controller
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/admin/analytics")
    public String analytics(Model model) {
        model.addAttribute("tripOccupancies", analyticsService.getTripOccupancies());
        model.addAttribute("demandByDate", analyticsService.getDemandByDate(14)); // последние 14 дней
        model.addAttribute("statusDist", analyticsService.getTicketStatusDistribution());
        return "admin/analytics";
    }
}
