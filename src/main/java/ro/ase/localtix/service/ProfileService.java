package ro.ase.localtix.service;

import ro.ase.localtix.model.User;

import java.util.List;

/**
 * Business logic for the profile page (US-3).
 */
public class ProfileService {

    /** Romanian phone format: "07" followed by exactly 8 digits. */
    public static final String PHONE_REGEX = "07\\d{8}";

    public static final String EMAIL_ALREADY_USED = "Email is already used by another account";
    public static final String INVALID_PHONE = "Phone number must have the format 07xxxxxxxx";
    public static final String PROFILE_UPDATED = "Profile updated successfully";

    /**
     * Updates the user's last name, first name, email and phone.
     * The data is validated first; the user is changed only if everything is valid.
     *
     * @param user           the user editing their profile
     * @param existingEmails the emails of all accounts in the system
     * @return the confirmation message shown after saving
     * @throws IllegalArgumentException if the email or the phone is invalid
     */
    public String update(User user, String newLastName, String newFirstName,
                         String newEmail, String newPhone, List<String> existingEmails) {
        validateEmail(user, newEmail, existingEmails);
        validatePhone(newPhone);

        user.setLastName(newLastName);
        user.setFirstName(newFirstName);
        user.setEmail(newEmail);
        user.setPhone(newPhone);

        return PROFILE_UPDATED;
    }

    /** The new email must not belong to another account (case-insensitive). */
    private void validateEmail(User user, String newEmail, List<String> existingEmails) {
        if (isOwnEmail(user, newEmail)) {
            return;
        }
        if (isEmailTaken(newEmail, existingEmails)) {
            throw new IllegalArgumentException(EMAIL_ALREADY_USED);
        }
    }

    private boolean isOwnEmail(User user, String email) {
        return email.equalsIgnoreCase(user.getEmail());
    }

    private boolean isEmailTaken(String email, List<String> existingEmails) {
        return existingEmails.stream().anyMatch(existing -> existing.equalsIgnoreCase(email));
    }

    /** The phone must follow the Romanian format 07xxxxxxxx. */
    private void validatePhone(String phone) {
        if (!phone.matches(PHONE_REGEX)) {
            throw new IllegalArgumentException(INVALID_PHONE);
        }
    }
}
