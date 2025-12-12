package ru.codeislive63.springmvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.codeislive63.springmvc.security.UserPrincipal;

@Controller
@RequiredArgsConstructor
public class AuthController {

    @GetMapping("/login")
    public String login(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserPrincipal principal,
                            Model model) {
        model.addAttribute("user", principal.user());
        return "dashboard";
    }
}
