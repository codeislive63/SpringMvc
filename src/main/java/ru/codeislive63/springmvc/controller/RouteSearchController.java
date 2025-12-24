package ru.codeislive63.springmvc.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.codeislive63.springmvc.domain.CarClass;
import ru.codeislive63.springmvc.domain.TrainType;
import ru.codeislive63.springmvc.repository.StationRepository;
import ru.codeislive63.springmvc.service.RouteSearchService;
import ru.codeislive63.springmvc.web.dto.RouteSearchRequest;

import java.util.Objects;

@Controller
@RequestMapping("/routes")
@RequiredArgsConstructor
public class RouteSearchController {

    private final StationRepository stationRepository;
    private final RouteSearchService routeSearchService;

    @GetMapping("/search")
    public String search(@Valid @ModelAttribute("req") RouteSearchRequest req,
                         BindingResult binding,
                         Model model) {
        model.addAttribute("stations", stationRepository.findAll());
        model.addAttribute("trainTypes", TrainType.values());
        model.addAttribute("carClasses", CarClass.values());

        if (req.getFromPointId() == null && req.getToPointId() == null
                && req.getDepartureDate() == null) {
            return "pages/routes/search";
        }

        if (binding.hasErrors()) {
            return "pages/routes/search";
        }

        if (Objects.equals(req.getFromPointId(), req.getToPointId())) {
            model.addAttribute("error", "Выберите разные станции отправления и назначения.");
            return "pages/routes/search";
        }

        model.addAttribute("req", req);
        model.addAttribute("itineraries", routeSearchService.search(req));
        return "pages/routes/results";
    }
}
