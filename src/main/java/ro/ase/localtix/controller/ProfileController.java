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
import ro.ase.localtix.service.ProfileService;

@Controller
public class ProfileController {

    /** Session key holding the id of the logged-in user (set by the login page, US-2). */
    public static final String SESSION_USER_ID = "userId";

    /** TEMPORARY: demo user from data.sql, used until login (US-2) exists. */
    private static final Long DEMO_USER_ID = 1L;

    private final UserRepository userRepository;
    private final ProfileService profileService = new ProfileService();

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        User user = getCurrentUser(session);
        model.addAttribute("user", user);
        fillForm(model, user.getLastName(), user.getFirstName(), user.getEmail(), user.getPhone());
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam String lastName,
                                @RequestParam String firstName,
                                @RequestParam String email,
                                @RequestParam String phone,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(session);
        try {
            String message = profileService.update(user, lastName.trim(), firstName.trim(),
                    email.trim(), phone.trim(), userRepository.findAllEmails());
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("successMessage", message);
            return "redirect:/profile";
        } catch (IllegalArgumentException e) {
            model.addAttribute("user", user);
            model.addAttribute("errorMessage", e.getMessage());
            fillForm(model, lastName, firstName, email, phone);
            return "profile";
        }
    }

    private User getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            userId = DEMO_USER_ID; // TEMPORARY until login exists (US-2)
        }
        return userRepository.findById(userId).orElseThrow();
    }

    /** Values shown in the edit form (kept as typed when there is an error). */
    private void fillForm(Model model, String lastName, String firstName, String email, String phone) {
        model.addAttribute("formLastName", lastName);
        model.addAttribute("formFirstName", firstName);
        model.addAttribute("formEmail", email);
        model.addAttribute("formPhone", phone);
    }
}