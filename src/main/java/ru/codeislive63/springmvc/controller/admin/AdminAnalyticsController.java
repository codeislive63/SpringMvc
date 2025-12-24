package ru.codeislive63.springmvc.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.ByteArrayResource;
import ru.codeislive63.springmvc.domain.TicketStatus;
import ru.codeislive63.springmvc.service.AnalyticsService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/admin/analytics")
    public String analytics(Model model) {
        var occupancies = analyticsService.getTripOccupancies();
        var demand = analyticsService.getDemandByDate(14);
        var statusDist = analyticsService.getTicketStatusDistribution();
        var revenue = analyticsService.getRevenueByRoute();
        var cancellations = analyticsService.getCancellationsByRoute();
        var routeDemand = analyticsService.getRouteDemand();

        List<String> ocLabels = occupancies.stream()
                .map(o -> o.routeLabel() + " (" +
                        o.trip().getDepartureTime().toLocalDate() + " " +
                        o.trip().getDepartureTime().toLocalTime().withSecond(0).withNano(0) + ")")
                .toList();
        List<Double> ocData = occupancies.stream().map(AnalyticsService.TripOccupancy::occupancy).toList();

        List<String> demandLabels = new ArrayList<>();
        List<Long> demandValues = new ArrayList<>();

        demand.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    demandLabels.add(entry.getKey().toString());
                    demandValues.add(entry.getValue());
                });


        List<String> statusLabels = statusDist.keySet().stream().map(TicketStatus::getLabel).toList();
        List<Long> statusValues = statusLabels.stream()
                .map(label -> Arrays.stream(TicketStatus.values())
                        .filter(s -> s.getLabel().equals(label))
                        .findFirst()
                        .map(statusDist::get)
                        .orElse(0L)
                ).toList();

        model.addAttribute("revenueLabels", revenue.keySet());
        model.addAttribute("revenueValues", revenue.values());
        model.addAttribute("cancelLabels", cancellations.keySet());
        model.addAttribute("cancelValues", cancellations.values());
        model.addAttribute("routeDemandLabels", routeDemand.keySet());
        model.addAttribute("routeDemandValues", routeDemand.values());
        model.addAttribute("ocLabels", ocLabels);
        model.addAttribute("ocData", ocData);
        model.addAttribute("demandLabels", demandLabels);
        model.addAttribute("demandValues", demandValues);
        model.addAttribute("statusLabels", statusLabels);
        model.addAttribute("statusValues", statusValues);
        return "pages/admin/analytics";
    }

    @GetMapping("/admin/analytics/report.xlsx")
    @ResponseBody
    public ResponseEntity<ByteArrayResource> downloadReport() {
        byte[] data = analyticsService.buildReport();
        var resource = new ByteArrayResource(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=analytics-report.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(data.length)
                .body(resource);
    }
}
