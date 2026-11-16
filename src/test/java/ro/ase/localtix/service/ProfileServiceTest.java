package ro.ase.localtix.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.ase.localtix.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProfileServiceTest {

    private ProfileService profileService;
    private User ana;
    private List<String> existingEmails;

    @BeforeEach
    void setUp() {
        profileService = new ProfileService();
        ana = new User("Popescu", "Ana", "ana@mail.com", "0712345678", LocalDate.of(2003, 5, 10));
        existingEmails = List.of("ana@mail.com", "maria@mail.com", "david@mail.com");
    }

    // AC2: name, phone and email can be changed
    @Test
    void namePhoneAndEmailAreUpdated() {
        profileService.update(ana, "Ionescu", "Ana-Maria",
                "ana.ionescu@mail.com", "0798765432", existingEmails);

        assertEquals("Ionescu", ana.getLastName());
        assertEquals("Ana-Maria", ana.getFirstName());
        assertEquals("ana.ionescu@mail.com", ana.getEmail());
        assertEquals("0798765432", ana.getPhone());
    }

    // AC1 + AC2: the birth date is displayed but not changed on update
    @Test
    void birthDateStaysUnchangedOnUpdate() {
        profileService.update(ana, "Ionescu", "Ana",
                "ana@mail.com", "0798765432", existingEmails);

        assertEquals("Ionescu", ana.getLastName());
        assertEquals(LocalDate.of(2003, 5, 10), ana.getBirthDate());
    }

    // AC3: the new email cannot already be used by another account
    @Test
    void emailUsedByAnotherAccountThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> profileService.update(ana, "Popescu", "Ana",
                        "maria@mail.com", "0712345678", existingEmails));

        assertEquals("Email is already used by another account", exception.getMessage());
    }

    // AC3: the email check ignores upper/lower case
    @Test
    void emailUsedByAnotherAccountWithUppercaseThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> profileService.update(ana, "Popescu", "Ana",
                        "Maria@Mail.com", "0712345678", existingEmails));
    }

    // AC3: the user can keep their own email
    @Test
    void keepingOwnEmailIsAllowed() {
        String message = profileService.update(ana, "Ionescu", "Ana",
                "ana@mail.com", "0712345678", existingEmails);

        assertEquals("Profile updated successfully", message);
        assertEquals("ana@mail.com", ana.getEmail());
    }

    // AC4: the new phone follows the Romanian format (07xxxxxxxx)
    @Test
    void phoneNotStartingWith07ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> profileService.update(ana, "Popescu", "Ana",
                        "ana@mail.com", "0812345678", existingEmails));

        assertEquals("Phone number must have the format 07xxxxxxxx", exception.getMessage());
    }

    // AC4: phone with too few digits
    @Test
    void phoneWithTooFewDigitsThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> profileService.update(ana, "Popescu", "Ana",
                        "ana@mail.com", "07123", existingEmails));
    }

    // AC4: phone containing letters
    @Test
    void phoneWithLettersThrowsException() {
        assertThrows(IllegalArgumentException.class,
                () -> profileService.update(ana, "Popescu", "Ana",
                        "ana@mail.com", "07abc45678", existingEmails));
    }

    // AC3 + AC4: on invalid data the profile stays unchanged
    @Test
    void invalidDataLeavesProfileUnchanged() {
        assertThrows(IllegalArgumentException.class,
                () -> profileService.update(ana, "Ionescu", "Ana",
                        "maria@mail.com", "0798765432", existingEmails));

        assertEquals("Popescu", ana.getLastName());
        assertEquals("ana@mail.com", ana.getEmail());
        assertEquals("0712345678", ana.getPhone());
    }

    // AC5: a confirmation message is shown after saving
    @Test
    void confirmationMessageIsReturnedAfterSaving() {
        String message = profileService.update(ana, "Ionescu", "Ana",
                "ana.ionescu@mail.com", "0798765432", existingEmails);

        assertEquals("Profile updated successfully", message);
    }
}