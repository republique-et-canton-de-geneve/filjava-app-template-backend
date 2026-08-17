package ch.ge.filjava.apptemplatebackend.ui.security;

import ch.ge.filjava.apptemplatebackend.domain.security.UtilisateurCourantProvider;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Component
public class SecurityUtilisateurCourantProvider implements UtilisateurCourantProvider {

    @Override
    public String getIdentifiant() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new IllegalStateException("Aucun utilisateur authentifié");
        }
        return authentication.getName();
    }
}
