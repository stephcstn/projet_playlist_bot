package fr.univ_poitiers.projet_playlist.repository;

import fr.univ_poitiers.projet_playlist.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
