package fr.univ_poitiers.projet_playlist.filter;

import fr.univ_poitiers.projet_playlist.model.SessionToken;
import fr.univ_poitiers.projet_playlist.model.User;
import fr.univ_poitiers.projet_playlist.repository.SessionTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SessionTokenFilter extends OncePerRequestFilter {

    private final SessionTokenRepository sessionTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String tokenValue = extractToken(request);

        if (tokenValue != null) {
            Optional<SessionToken> optToken = sessionTokenRepository.findByToken(tokenValue);

            if (optToken.isPresent()) {
                SessionToken sessionToken = optToken.get();

                // Vérifier si le token est expiré
                if (!sessionToken.isExpired()) {
                    User user = sessionToken.getUser();

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    user.getAuthorities()
                            );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    sessionTokenRepository.delete(sessionToken);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Récupère le token soit depuis l'en-tête "Authorization: Bearer <token>" (pour le Bot Discord / API),
     * soit depuis le cookie "AUTH_TOKEN" (pour la navigation sur le site web).
     */
    private String extractToken(HttpServletRequest request) {
        // Header Authorization (requêtes du bot Discord)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // Cookie du navigateur
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> "AUTH_TOKEN".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }
}
