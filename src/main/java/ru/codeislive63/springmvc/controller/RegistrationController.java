package ru.codeislive63.springmvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.codeislive63.springmvc.domain.RoleType;
import ru.codeislive63.springmvc.service.UserService;

@Controller
@RequiredArgsConstructor
public class RegistrationController {

    private final UserService userService;

    @GetMapping("/register")
    public String showRegisterForm() {
        return "pages/auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String fullName,
                           @RequestParam String email,
                           @RequestParam String password,
                           Model model) {
        try {
            userService.getByEmail(email);
            model.addAttribute("error", "Пользователь с таким email уже существует");
            return "pages/auth/register";
        } catch (IllegalArgumentException ignored) { }

        userService.register(email, password, fullName, RoleType.CUSTOMER);
        model.addAttribute("success", "Регистрация прошла успешно, войдите с вашими данными");
        return "pages/auth/login";
    }
}
