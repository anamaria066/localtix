package ro.ase.localtix.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.ase.localtix.model.User;
import ro.ase.localtix.repository.UserRepository;
import ro.ase.localtix.service.AuthService;

import java.time.LocalDateTime;

/**
 * Login and logout pages (US-2).
 * The logged-in user's id is kept in the HTTP session under ProfileController.SESSION_USER_ID.
 */
@Controller
public class LoginController {

    public static final String LOGGED_OUT = "You have been logged out.";

    private final UserRepository userRepository;
    private final AuthService authService = new AuthService();

    public LoginController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/login")
    public String showLogin(HttpSession session) {
        if (session.getAttribute(ProfileController.SESSION_USER_ID) != null) {
            return "redirect:/profile"; // already logged in
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        User user = userRepository.findByEmailIgnoreCase(email.trim()).orElse(null);
        try {
            authService.login(user, password, LocalDateTime.now());
            userRepository.save(user); // failed attempts were reset
            session.setAttribute(ProfileController.SESSION_USER_ID, user.getId());
            return "redirect:/profile";
        } catch (IllegalArgumentException e) {
            if (user != null) {
                userRepository.save(user); // keep the failed attempts / lock
            }
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("formEmail", email);
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("successMessage", LOGGED_OUT);
        return "redirect:/login";
    }
}
