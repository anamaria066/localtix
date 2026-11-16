package ro.ase.localtix.service;

import java.time.LocalDate;

/**
 * The data typed in the registration form (US-1).
 * It is not an entity: after validation, a User is created from it.
 */
public class RegistrationForm {

    private String lastName;
    private String firstName;
    private String email;
    private String phone;
    private LocalDate birthDate;
    private String password;
    private String confirmPassword;

    public RegistrationForm() {
    }

    public RegistrationForm(String lastName, String firstName, String email, String phone,
                            LocalDate birthDate, String password, String confirmPassword) {
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;
        this.phone = phone;
        this.birthDate = birthDate;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
