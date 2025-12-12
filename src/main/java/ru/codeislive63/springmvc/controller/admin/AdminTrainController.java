package ru.codeislive63.springmvc.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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
        return "admin/trains/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("train", new Train());
        return "admin/trains/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Train train = trainRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Поезд не найден"));

        model.addAttribute("train", train);
        return "admin/trains/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute Train train) {
        trainRepository.save(train);
        return "redirect:/admin/panel/trains";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        trainRepository.deleteById(id);
        return "redirect:/admin/panel/trains";
    }
}
