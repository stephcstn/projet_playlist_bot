package fr.univ_poitiers.projet_playlist.security;

import fr.univ_poitiers.projet_playlist.model.SessionToken;
import fr.univ_poitiers.projet_playlist.model.User;
import fr.univ_poitiers.projet_playlist.service.SessionTokenService;
import fr.univ_poitiers.projet_playlist.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    private SessionTokenService sessionTokenService;

    @Autowired
    private UserService userService;

    private static final String GUILD_ID = "1567927102177546341";
    private static final String ADMIN_ROLE_ID = "1567927663077752913";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String username = oauthUser.getAttribute("username");
        String discordId = oauthUser.getAttribute("id");

        // 1. Récupérer le jeton d'accès OAuth2 Discord émis par Discord
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                oauthToken.getAuthorizedClientRegistrationId(),
                oauthToken.getName()
        );
        String accessToken = client.getAccessToken().getTokenValue();

        // Interroger l'API Discord pour récupérer les rôles du serveur
        boolean isAdmin = checkDiscordRole(accessToken);

        String role = isAdmin ? "ADMIN" : "USER";
        User user = userService.saveUser(username, role, discordId);

        SessionToken sessionToken = sessionTokenService.createSession(user);

        Cookie cookie = new Cookie("AUTH_TOKEN", sessionToken.getToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);

        response.sendRedirect("/");
    }

    private boolean checkDiscordRole(String accessToken) {
        RestClient restClient = RestClient.create();

        try {
            Map<String, Object> memberData = restClient.get()
                    .uri("https://discord.com/api/users/@me/guilds/{guildId}/member", GUILD_ID)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (memberData != null && memberData.containsKey("roles")) {
                List<String> roleIds = (List<String>) memberData.get("roles");
                return roleIds.contains(ADMIN_ROLE_ID);
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }
}
