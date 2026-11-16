package ro.ase.localtix.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ro.ase.localtix.model.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u.email from User u")
    List<String> findAllEmails();
}