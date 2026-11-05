package fr.univ_poitiers.projet_playlist.repository;

import fr.univ_poitiers.projet_playlist.model.SessionToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SessionTokenRepository extends JpaRepository<SessionToken, Long> {
    Optional<SessionToken> findByToken(String token);
}
