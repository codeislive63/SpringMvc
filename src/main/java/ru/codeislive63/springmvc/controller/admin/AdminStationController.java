package ru.codeislive63.springmvc.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.codeislive63.springmvc.domain.entity.Station;
import ru.codeislive63.springmvc.repository.StationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/panel/stations")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminStationController {

    private final StationRepository stationRepository;

    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String q, Model model) {
        List<Station> stations = stationRepository.findAll();

        if (q != null && !q.isBlank()) {
            String s = q.toLowerCase();
            stations = stations.stream()
                    .filter(st ->
                            (st.getName() != null && st.getName().toLowerCase().contains(s)) ||
                                    (st.getCode() != null && st.getCode().toLowerCase().contains(s))
                    )
                    .collect(Collectors.toList());
        }

        model.addAttribute("stations", stations);
        model.addAttribute("q", q == null ? "" : q);
        return "pages/admin/stations/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("station", new Station());
        return "pages/admin/stations/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Station station = stationRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Станция не найдена"));

        model.addAttribute("station", station);
        return "pages/admin/stations/form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute Station station, RedirectAttributes ra) {
        try {
            stationRepository.save(station);
            ra.addFlashAttribute("success",
                    station.getId() == null ? "Станция создана" : "Станция сохранена");
        } catch (DataIntegrityViolationException ex) {
            ra.addFlashAttribute("error",
                    "Нельзя сохранить станцию: проверьте уникальность кода и корректность данных.");
        }
        return "redirect:/admin/panel/stations";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            stationRepository.deleteById(id);
            ra.addFlashAttribute("success", "Станция удалена");
        } catch (DataIntegrityViolationException ex) {
            ra.addFlashAttribute("error",
                    "Нельзя удалить станцию: она используется в маршрутах. Сначала удалите маршруты/рейсы, связанные с этой станцией.");
        }
        return "redirect:/admin/panel/stations";
    }
}
