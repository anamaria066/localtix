package ro.ase.localtix.service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Validates the registration form (US-1).
 */
public class RegistrationValidator {

    // ----- limits -----
    public static final int MINIMUM_AGE = 14;
    public static final int MINIMUM_PASSWORD_LENGTH = 8;

    // ----- formats -----
    /** Text without spaces, one "@", a domain and an extension (e.g. david@localtix.ro). */
    public static final String EMAIL_REGEX = "[^@\\s]+@[^@\\s]+\\.[^@\\s]+";
    private static final String UPPERCASE_REGEX = ".*[A-Z].*";
    private static final String DIGIT_REGEX = ".*\\d.*";
    private static final String SYMBOL_REGEX = ".*[^A-Za-z0-9].*";

    // ----- error messages -----
    public static final String LAST_NAME_REQUIRED = "Last name is required";
    public static final String FIRST_NAME_REQUIRED = "First name is required";
    public static final String EMAIL_REQUIRED = "Email is required";
    public static final String PHONE_REQUIRED = "Phone number is required";
    public static final String BIRTH_DATE_REQUIRED = "Birth date is required";
    public static final String PASSWORD_REQUIRED = "Password is required";
    public static final String CONFIRM_PASSWORD_REQUIRED = "Password confirmation is required";

    public static final String INVALID_EMAIL = "Email must have a valid format";
    public static final String TOO_YOUNG = "You must be at least " + MINIMUM_AGE + " years old";
    public static final String WEAK_PASSWORD = "Password must have at least " + MINIMUM_PASSWORD_LENGTH
            + " characters, one uppercase letter, one digit and one symbol";
    public static final String PASSWORDS_DO_NOT_MATCH = "Passwords do not match";

    /**
     * Validates every field of the form.
     *
     * @param form           the data typed by the visitor
     * @param existingEmails the emails of all accounts in the system
     * @param today          the current date (used for the minimum age rule)
     * @return the errors, as field name -> message; empty if the data is valid
     */
    public Map<String, String> validate(RegistrationForm form, List<String> existingEmails, LocalDate today) {
        Map<String, String> errors = new LinkedHashMap<>();

        addError(errors, "lastName", validateRequired(form.getLastName(), LAST_NAME_REQUIRED));
        addError(errors, "firstName", validateRequired(form.getFirstName(), FIRST_NAME_REQUIRED));
        addError(errors, "email", validateEmail(form.getEmail(), existingEmails));
        addError(errors, "phone", validatePhone(form.getPhone()));
        addError(errors, "birthDate", validateBirthDate(form.getBirthDate(), today));
        addError(errors, "password", validatePassword(form.getPassword()));
        addError(errors, "confirmPassword",
                validateConfirmPassword(form.getPassword(), form.getConfirmPassword()));

        return errors;
    }

    // Each validateX method returns the error message, or null if the field is valid.

    private String validateRequired(String value, String requiredMessage) {
        return isBlank(value) ? requiredMessage : null;
    }

    private String validateEmail(String email, List<String> existingEmails) {
        if (isBlank(email)) {
            return EMAIL_REQUIRED;
        }
        if (!email.matches(EMAIL_REGEX)) {
            return INVALID_EMAIL;
        }
        if (isEmailTaken(email, existingEmails)) {
            return ProfileService.EMAIL_ALREADY_USED;
        }
        return null;
    }

    private String validatePhone(String phone) {
        if (isBlank(phone)) {
            return PHONE_REQUIRED;
        }
        if (!phone.matches(ProfileService.PHONE_REGEX)) {
            return ProfileService.INVALID_PHONE;
        }
        return null;
    }

    private String validateBirthDate(LocalDate birthDate, LocalDate today) {
        if (birthDate == null) {
            return BIRTH_DATE_REQUIRED;
        }
        if (isYoungerThanMinimumAge(birthDate, today)) {
            return TOO_YOUNG;
        }
        return null;
    }

    private String validatePassword(String password) {
        if (isBlank(password)) {
            return PASSWORD_REQUIRED;
        }
        if (!isStrongPassword(password)) {
            return WEAK_PASSWORD;
        }
        return null;
    }

    private String validateConfirmPassword(String password, String confirmPassword) {
        if (isBlank(confirmPassword)) {
            return CONFIRM_PASSWORD_REQUIRED;
        }
        if (!confirmPassword.equals(password)) {
            return PASSWORDS_DO_NOT_MATCH;
        }
        return null;
    }

    // ----- helper rules -----

    private boolean isEmailTaken(String email, List<String> existingEmails) {
        return existingEmails.stream().anyMatch(existing -> existing.equalsIgnoreCase(email));
    }

    /** Someone who turns the minimum age exactly today is accepted. */
    private boolean isYoungerThanMinimumAge(LocalDate birthDate, LocalDate today) {
        return birthDate.plusYears(MINIMUM_AGE).isAfter(today);
    }

    private boolean isStrongPassword(String password) {
        return password.length() >= MINIMUM_PASSWORD_LENGTH
                && password.matches(UPPERCASE_REGEX)
                && password.matches(DIGIT_REGEX)
                && password.matches(SYMBOL_REGEX);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void addError(Map<String, String> errors, String field, String message) {
        if (message != null) {
            errors.put(field, message);
        }
    }
}
