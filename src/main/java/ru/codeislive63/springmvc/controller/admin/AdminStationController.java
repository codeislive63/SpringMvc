package ru.codeislive63.springmvc.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.codeislive63.springmvc.domain.entity.Station;
import ru.codeislive63.springmvc.repository.StationRepository;

@Controller
@RequestMapping("/admin/panel/stations")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminStationController {

    private final StationRepository stationRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("stations", stationRepository.findAll());
        return "admin/stations/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("station", new Station());
        return "admin/stations/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Station station = stationRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Станция не найдена"));

        model.addAttribute("station", station);
        return "admin/stations/form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute Station station) {
        stationRepository.save(station);
        return "redirect:/admin/panel/stations";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        stationRepository.deleteById(id);
        return "redirect:/admin/panel/stations";
    }
}
