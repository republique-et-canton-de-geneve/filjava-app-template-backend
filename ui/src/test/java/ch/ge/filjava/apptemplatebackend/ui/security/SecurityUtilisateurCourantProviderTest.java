package ch.ge.filjava.apptemplatebackend.ui.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecurityUtilisateurCourantProviderTest {

    private final SecurityUtilisateurCourantProvider provider = new SecurityUtilisateurCourantProvider();

    @AfterEach
    void nettoyerContexte() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void devraitRetournerIdentifiantUtilisateurAuthentifie() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("user-123", null, "ROLE_USER")
        );

        assertEquals("user-123", provider.getIdentifiant());
    }

    @Test
    void devraitRefuserAbsenceUtilisateurAuthentifie() {
        assertThrows(IllegalStateException.class, provider::getIdentifiant);
    }
}
