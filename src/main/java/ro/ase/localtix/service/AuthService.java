package ro.ase.localtix.service;

import ro.ase.localtix.model.User;

import java.time.LocalDateTime;

/**
 * Login logic (US-2).
 * Receives the user found by email (or null if there is no account with that email)
 * and the current time as a parameter, so the lock rule can be tested.
 */
public class AuthService {

    /** After this many consecutive failed attempts the account is locked. */
    public static final int MAX_FAILED_ATTEMPTS = 3;

    /** How long the account stays locked. */
    public static final int LOCK_DURATION_MINUTES = 15;

    public static final String INVALID_CREDENTIALS = "Invalid email or password";
    public static final String ACCOUNT_LOCKED =
            "Account locked. Please try again in " + LOCK_DURATION_MINUTES + " minutes.";

    /**
     * Logs the user in.
     *
     * @param user     the user found by email, or null if no account exists
     * @param password the password typed in the login form
     * @param now      the current time
     * @return the logged-in user
     * @throws IllegalArgumentException if the credentials are wrong or the account is locked
     */
    public User login(User user, String password, LocalDateTime now) {
        if (user == null) {
            throw new IllegalArgumentException(INVALID_CREDENTIALS);
        }
        if (isLocked(user, now)) {
            throw new IllegalArgumentException(ACCOUNT_LOCKED);
        }
        if (!isPasswordCorrect(user, password)) {
            registerFailedAttempt(user, now);
            throw new IllegalArgumentException(INVALID_CREDENTIALS);
        }

        resetFailedAttempts(user);
        return user;
    }

    private boolean isLocked(User user, LocalDateTime now) {
        return user.getLockedUntil() != null && now.isBefore(user.getLockedUntil());
    }

    private boolean isPasswordCorrect(User user, String password) {
        return user.getPassword().equals(password);
    }

    /** Counts a failed attempt; the last allowed one locks the account. */
    private void registerFailedAttempt(User user, LocalDateTime now) {
        user.setFailedAttempts(user.getFailedAttempts() + 1);
        if (user.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
            user.setLockedUntil(now.plusMinutes(LOCK_DURATION_MINUTES));
            user.setFailedAttempts(0);
        }
    }

    private void resetFailedAttempts(User user) {
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
    }
}
