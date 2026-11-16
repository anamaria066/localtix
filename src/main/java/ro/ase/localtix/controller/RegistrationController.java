package ro.ase.localtix.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ro.ase.localtix.model.User;
import ro.ase.localtix.repository.UserRepository;
import ro.ase.localtix.service.RegistrationForm;
import ro.ase.localtix.service.RegistrationValidator;

import java.time.LocalDate;
import java.util.Map;

/**
 * Registration page (US-1).
 * The data is checked by RegistrationValidator; if it is valid, a new User is saved
 * and the visitor is sent to the login page.
 */
@Controller
public class RegistrationController {

    public static final String ACCOUNT_CREATED = "Account created successfully. You can now log in.";

    private final UserRepository userRepository;
    private final RegistrationValidator registrationValidator = new RegistrationValidator();

    public RegistrationController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/register")
    public String showRegister(HttpSession session, Model model) {
        if (session.getAttribute(ProfileController.SESSION_USER_ID) != null) {
            return "redirect:/profile"; // already logged in
        }
        model.addAttribute("form", new RegistrationForm());
        model.addAttribute("errors", Map.of());
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String lastName,
                           @RequestParam String firstName,
                           @RequestParam String email,
                           @RequestParam String phone,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthDate,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        RegistrationForm form = new RegistrationForm(lastName.trim(), firstName.trim(), email.trim(),
                phone.trim(), birthDate, password, confirmPassword);

        Map<String, String> errors =
                registrationValidator.validate(form, userRepository.findAllEmails(), LocalDate.now());

        if (!errors.isEmpty()) {
            form.setPassword("");        // passwords are never sent back to the page
            form.setConfirmPassword("");
            model.addAttribute("form", form);
            model.addAttribute("errors", errors);
            return "register";
        }

        userRepository.save(createUser(form));
        redirectAttributes.addFlashAttribute("successMessage", ACCOUNT_CREATED);
        return "redirect:/login";
    }

    private User createUser(RegistrationForm form) {
        User user = new User(form.getLastName(), form.getFirstName(), form.getEmail(),
                form.getPhone(), form.getBirthDate());
        user.setPassword(form.getPassword());
        return user;
    }
}
