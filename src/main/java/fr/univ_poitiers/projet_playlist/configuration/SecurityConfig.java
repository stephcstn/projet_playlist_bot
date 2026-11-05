package fr.univ_poitiers.projet_playlist.configuration;

import fr.univ_poitiers.projet_playlist.filter.SessionTokenFilter;
import fr.univ_poitiers.projet_playlist.security.OAuth2LoginSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private SessionTokenFilter sessionTokenFilter;

    @Autowired
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)

                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        // Console H2
                        .requestMatchers("/h2-console/**").permitAll()

                        // Ressources statiques et accueil
                        .requestMatchers("/", "/index.html", "/error", "/css/**", "/js/**").permitAll()

                        // Endpoints OAuth2 et connexion
                        .requestMatchers("/login/**", "/oauth2/**").permitAll()

                        // Endpoint public de test
                        .requestMatchers("/api/test/public").permitAll()

                        // Rôle Admin
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Tout le reste nécessite le token de session
                        .anyRequest().authenticated()
                )

                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2LoginSuccessHandler)
                )

                .addFilterBefore(sessionTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}