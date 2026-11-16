package ro.ase.localtix.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ro.ase.localtix.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private static final String CORRECT_PASSWORD = "Parola123!";
    private static final String WRONG_PASSWORD = "Wrong123!";
    private static final String INVALID_CREDENTIALS = "Invalid email or password";
    private static final String ACCOUNT_LOCKED = "Account locked. Please try again in 15 minutes.";

    private final LocalDateTime now = LocalDateTime.of(2026, 11, 16, 12, 0);

    private AuthService authService;
    private User maria;

    @BeforeEach
    void setUp() {
        authService = new AuthService();
        maria = new User("Ionescu", "Maria", "maria@mail.com", "0723456789", LocalDate.of(2002, 11, 21));
        maria.setPassword(CORRECT_PASSWORD);
    }

    /** A failed login whose exception we ignore - we only care about its effect on the account. */
    private void failedLogin(LocalDateTime moment) {
        try {
            authService.login(maria, WRONG_PASSWORD, moment);
        } catch (RuntimeException ignored) {
            // expected
        }
    }

    // AC1: with the correct email and password the user logs in
    @Test
    void correctCredentialsReturnTheUser() {
        User result = authService.login(maria, CORRECT_PASSWORD, now);

        assertSame(maria, result);
    }

    // AC2: wrong password -> generic message
    @Test
    void wrongPasswordShowsGenericMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(maria, WRONG_PASSWORD, now));

        assertEquals(INVALID_CREDENTIALS, ex.getMessage());
    }

    // AC2: missing account -> the same generic message
    @Test
    void missingAccountShowsSameGenericMessage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(null, CORRECT_PASSWORD, now));

        assertEquals(INVALID_CREDENTIALS, ex.getMessage());
    }

    // AC3: two failed attempts do not lock the account yet
    @Test
    void twoFailedAttemptsDoNotLockTheAccount() {
        failedLogin(now);
        failedLogin(now);

        assertEquals(2, maria.getFailedAttempts());
        assertNull(maria.getLockedUntil());
    }

    // AC3: after 3 consecutive failed attempts the account is locked for 15 minutes
    @Test
    void threeFailedAttemptsLockTheAccountFor15Minutes() {
        failedLogin(now);
        failedLogin(now);
        failedLogin(now);

        assertEquals(now.plusMinutes(15), maria.getLockedUntil());
    }

    // AC3: while the account is locked, even the correct password is refused
    @Test
    void lockedAccountRefusesEvenTheCorrectPassword() {
        failedLogin(now);
        failedLogin(now);
        failedLogin(now);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(maria, CORRECT_PASSWORD, now.plusMinutes(14)));

        assertEquals(ACCOUNT_LOCKED, ex.getMessage());
    }

    // AC3: after the 15 minutes the account is unlocked
    @Test
    void accountIsUnlockedAfter15Minutes() {
        failedLogin(now);
        failedLogin(now);
        failedLogin(now);

        User result = authService.login(maria, CORRECT_PASSWORD, now.plusMinutes(15));

        assertSame(maria, result);
        assertEquals(0, maria.getFailedAttempts());
        assertNull(maria.getLockedUntil());
    }

    // AC4: a successful login resets the number of failed attempts
    @Test
    void successfulLoginResetsFailedAttempts() {
        failedLogin(now);
        failedLogin(now);

        User result = authService.login(maria, CORRECT_PASSWORD, now);

        assertSame(maria, result);
        assertEquals(0, maria.getFailedAttempts());
    }

    // AC4: after a reset, failed attempts are counted again from zero
    @Test
    void nonConsecutiveFailedAttemptsDoNotLockTheAccount() {
        failedLogin(now);
        failedLogin(now);
        authService.login(maria, CORRECT_PASSWORD, now);
        failedLogin(now);
        failedLogin(now);

        User result = authService.login(maria, CORRECT_PASSWORD, now);

        assertSame(maria, result);
        assertNull(maria.getLockedUntil());
    }
}
