package ru.codeislive63.springmvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "redirect:/routes/search";
    }

    @GetMapping({"/trips/search", "/search"})
    public String redirectToRoutes() {
        return "redirect:/routes/search";
    }
}
