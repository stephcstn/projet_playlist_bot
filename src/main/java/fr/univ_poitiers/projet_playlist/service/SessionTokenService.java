package fr.univ_poitiers.projet_playlist.service;

import fr.univ_poitiers.projet_playlist.model.SessionToken;
import fr.univ_poitiers.projet_playlist.model.User;
import fr.univ_poitiers.projet_playlist.repository.SessionTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class SessionTokenService {
    @Autowired
    private SessionTokenRepository sessionTokenRepository;

    public SessionToken createSession(User user){
        SessionToken sessionToken = new SessionToken();
        sessionToken.setToken(UUID.randomUUID().toString());
        sessionToken.setUser(user);
        sessionToken.setCreatedAt(Instant.now());
        sessionToken.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        sessionTokenRepository.save(sessionToken);

        return sessionToken;
    }
}
