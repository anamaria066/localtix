package ro.ase.localtix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ro.ase.localtix.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u.email from User u")
    List<String> findAllEmails();

    /** Used by the login page (US-2). */
    Optional<User> findByEmailIgnoreCase(String email);
}
