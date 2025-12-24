package ru.codeislive63.springmvc.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.codeislive63.springmvc.domain.entity.Train;
import ru.codeislive63.springmvc.repository.TrainRepository;

@Controller
@RequestMapping("/admin/panel/trains")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminTrainController {

    private final TrainRepository trainRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("trains", trainRepository.findAll());
        return "pages/admin/trains/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("train", new Train());
        return "pages/admin/trains/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Train train = trainRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Поезд не найден"));

        model.addAttribute("train", train);
        return "pages/admin/trains/form";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute Train train, RedirectAttributes ra) {
        try {
            trainRepository.save(train);
            ra.addFlashAttribute("success", "Поезд сохранён");
        } catch (DataIntegrityViolationException ex) {
            ra.addFlashAttribute("error",
                    "Нельзя сохранить поезд: проверьте уникальность кода и корректность данных.");
        }
        return "redirect:/admin/panel/trains";
    }


    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            trainRepository.deleteById(id);
            ra.addFlashAttribute("success", "Поезд удалён");
        } catch (DataIntegrityViolationException ex) {
            ra.addFlashAttribute("error",
                    "Нельзя удалить поезд: он используется в рейсах. Сначала удалите связанные рейсы.");
        }
        return "redirect:/admin/panel/trains";
    }
}
