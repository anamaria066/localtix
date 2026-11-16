package ro.ase.localtix.service;

import ro.ase.localtix.model.User;

import java.util.List;

/**
 * Business logic for the profile page (US-3).
 */
public class ProfileService {

    /**
     * Updates the user's last name, first name, email and phone.
     *
     * @param user           the user editing their profile
     * @param existingEmails the emails of all accounts in the system
     * @return the confirmation message shown after saving
     */
    public String update(User user, String newLastName, String newFirstName,
                         String newEmail, String newPhone, List<String> existingEmails) {
        boolean emailChanged = !newEmail.equalsIgnoreCase(user.getEmail());
        if (emailChanged) {
            for (String email : existingEmails) {
                if (email.equalsIgnoreCase(newEmail)) {
                    throw new IllegalArgumentException("Email is already used by another account");
                }
            }
        }

        if (!newPhone.matches("07\\d{8}")) {
            throw new IllegalArgumentException("Phone number must have the format 07xxxxxxxx");
        }

        user.setLastName(newLastName);
        user.setFirstName(newFirstName);
        user.setEmail(newEmail);
        user.setPhone(newPhone);

        return "Profile updated successfully";
    }
}