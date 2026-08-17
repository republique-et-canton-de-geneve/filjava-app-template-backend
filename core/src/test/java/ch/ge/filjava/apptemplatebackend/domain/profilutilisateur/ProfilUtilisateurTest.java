package ch.ge.filjava.apptemplatebackend.domain.profilutilisateur;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProfilUtilisateurTest {

    @Test
    void rejectsNullIdentifier() {
        LocalDate dateNaissance = LocalDate.of(2000, 1, 1);
        assertThrows(
                IllegalArgumentException.class,
                () -> new ProfilUtilisateur(null, dateNaissance)
        );
    }

    @Test
    void rejectsBlankIdentifier() {
        LocalDate dateNaissance = LocalDate.of(2000, 1, 1);
        assertThrows(
                IllegalArgumentException.class,
                () -> new ProfilUtilisateur("   ", dateNaissance)
        );
    }

    @Test
    void rejectsNullBirthDate() {
        assertThrows(
                NullPointerException.class,
                () -> new ProfilUtilisateur("user-123", null)
        );
    }

    @Test
    void rejectsFutureBirthDate() {
        LocalDate dateReference = LocalDate.of(2026, 6, 25);
        LocalDate futureDate = LocalDate.of(2026, 6, 26);

        assertThrows(
                IllegalArgumentException.class,
                () -> new ProfilUtilisateur("user-123", futureDate, dateReference)
        );
    }

    @Test
    void acceptsValidProfile() {
        assertDoesNotThrow(() ->
                new ProfilUtilisateur("user-123", LocalDate.of(2000, 1, 1))
        );
    }
}