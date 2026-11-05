package fr.univ_poitiers.projet_playlist.service;

import fr.univ_poitiers.projet_playlist.model.User;
import fr.univ_poitiers.projet_playlist.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User saveUser(String username, String role, String discordId) {
        return userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(username);
                    newUser.setRole(role);
                    newUser.setDiscordId(discordId);
                    return userRepository.save(newUser);
                });
    }
}
