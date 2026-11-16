package ro.ase.localtix.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationValidatorTest {

    private static final String INVALID_EMAIL = "Email must have a valid format";
    private static final String EMAIL_ALREADY_USED = "Email is already used by another account";
    private static final String INVALID_PHONE = "Phone number must have the format 07xxxxxxxx";
    private static final String TOO_YOUNG = "You must be at least 14 years old";
    private static final String WEAK_PASSWORD =
            "Password must have at least 8 characters, one uppercase letter, one digit and one symbol";
    private static final String PASSWORDS_DO_NOT_MATCH = "Passwords do not match";

    private final LocalDate today = LocalDate.of(2026, 11, 16);

    private RegistrationValidator validator;
    private List<String> existingEmails;
    private RegistrationForm form;

    @BeforeEach
    void setUp() {
        validator = new RegistrationValidator();
        existingEmails = List.of("ana@localtix.ro", "maria@localtix.ro");
        // a completely valid form; each test changes only what it checks
        form = new RegistrationForm("Popa", "David", "david@localtix.ro", "0745123456",
                LocalDate.of(2004, 3, 15), "Parola123!", "Parola123!");
    }

    private Map<String, String> validate() {
        return validator.validate(form, existingEmails, today);
    }

    // All ACs: valid data produces no errors
    @Test
    void validFormHasNoErrors() {
        assertTrue(validate().isEmpty());
    }

    // AC1: every field is required
    @Test
    void emptyFormHasARequiredErrorForEveryField() {
        form = new RegistrationForm("", "", "", "", null, "", "");

        Map<String, String> errors = validate();

        assertEquals("Last name is required", errors.get("lastName"));
        assertEquals("First name is required", errors.get("firstName"));
        assertEquals("Email is required", errors.get("email"));
        assertEquals("Phone number is required", errors.get("phone"));
        assertEquals("Birth date is required", errors.get("birthDate"));
        assertEquals("Password is required", errors.get("password"));
        assertEquals("Password confirmation is required", errors.get("confirmPassword"));
    }

    // AC1: a field with only spaces counts as empty
    @Test
    void blankLastNameIsRequired() {
        form.setLastName("   ");

        assertEquals("Last name is required", validate().get("lastName"));
    }

    // AC2: the email must have a valid format
    @Test
    void emailWithoutAtSignIsInvalid() {
        form.setEmail("david.localtix.ro");

        assertEquals(INVALID_EMAIL, validate().get("email"));
    }

    // AC2: the email must have a valid format (missing domain)
    @Test
    void emailWithoutDomainIsInvalid() {
        form.setEmail("david@");

        assertEquals(INVALID_EMAIL, validate().get("email"));
    }

    // AC2: the email must not already exist in the system
    @Test
    void existingEmailIsRejected() {
        form.setEmail("ana@localtix.ro");

        assertEquals(EMAIL_ALREADY_USED, validate().get("email"));
    }

    // AC2: the existing-email check ignores upper/lower case
    @Test
    void existingEmailWithDifferentCaseIsRejected() {
        form.setEmail("Ana@LocalTix.ro");

        assertEquals(EMAIL_ALREADY_USED, validate().get("email"));
    }

    // AC3: the phone must start with 07
    @Test
    void phoneNotStartingWith07IsInvalid() {
        form.setPhone("0812345678");

        assertEquals(INVALID_PHONE, validate().get("phone"));
    }

    // AC3: the phone must have exactly 10 digits
    @Test
    void phoneWithTooFewDigitsIsInvalid() {
        form.setPhone("07451234");

        assertEquals(INVALID_PHONE, validate().get("phone"));
    }

    // AC4: a user younger than 14 cannot register
    @Test
    void userYoungerThan14IsRejected() {
        form.setBirthDate(today.minusYears(14).plusDays(1)); // turns 14 tomorrow

        assertEquals(TOO_YOUNG, validate().get("birthDate"));
    }

    // AC4: a user who turns 14 exactly today can register
    @Test
    void userTurning14TodayIsAccepted() {
        form.setBirthDate(today.minusYears(14));

        assertFalse(validate().containsKey("birthDate"));
    }

    // AC5: the password has at least 8 characters
    @Test
    void passwordShorterThan8CharactersIsWeak() {
        form.setPassword("Pa12!");
        form.setConfirmPassword("Pa12!");

        assertEquals(WEAK_PASSWORD, validate().get("password"));
    }

    // AC5: the password has at least one uppercase letter
    @Test
    void passwordWithoutUppercaseIsWeak() {
        form.setPassword("parola123!");
        form.setConfirmPassword("parola123!");

        assertEquals(WEAK_PASSWORD, validate().get("password"));
    }

    // AC5: the password has at least one digit
    @Test
    void passwordWithoutDigitIsWeak() {
        form.setPassword("ParolaMea!");
        form.setConfirmPassword("ParolaMea!");

        assertEquals(WEAK_PASSWORD, validate().get("password"));
    }

    // AC5: the password has at least one symbol
    @Test
    void passwordWithoutSymbolIsWeak() {
        form.setPassword("Parola1234");
        form.setConfirmPassword("Parola1234");

        assertEquals(WEAK_PASSWORD, validate().get("password"));
    }

    // AC6: the password and its confirmation must match
    @Test
    void differentConfirmationIsRejected() {
        form.setConfirmPassword("Parola124!");

        assertEquals(PASSWORDS_DO_NOT_MATCH, validate().get("confirmPassword"));
    }

    // AC7: every invalid field gets its own error message
    @Test
    void eachInvalidFieldGetsItsOwnError() {
        form.setEmail("maria@localtix.ro");
        form.setPhone("123");
        form.setPassword("abc");
        form.setConfirmPassword("xyz");

        Map<String, String> errors = validate();

        assertEquals(4, errors.size());
        assertEquals(EMAIL_ALREADY_USED, errors.get("email"));
        assertEquals(INVALID_PHONE, errors.get("phone"));
        assertEquals(WEAK_PASSWORD, errors.get("password"));
        assertEquals(PASSWORDS_DO_NOT_MATCH, errors.get("confirmPassword"));
    }
}
