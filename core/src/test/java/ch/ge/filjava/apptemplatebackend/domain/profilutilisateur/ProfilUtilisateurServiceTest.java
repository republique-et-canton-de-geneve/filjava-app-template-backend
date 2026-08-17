package ch.ge.filjava.apptemplatebackend.domain.profilutilisateur;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProfilUtilisateurServiceTest {

    private static final Clock CLOCK = Clock.fixed(
        Instant.parse("2026-06-25T00:00:00Z"),
        ZoneOffset.UTC
    );

    private final RepositoryMemoire repository = new RepositoryMemoire();
    private final ProfilUtilisateurService service = new ProfilUtilisateurService(repository, CLOCK);
    private final String identifiant = "user-123";

    @Test
    void considersUserAdultOnEighteenthBirthday() {
        service.enregistrerDateNaissance(identifiant, LocalDate.of(2008, 6, 25));

        assertEquals(ProfilUtilisateur.StatutMajorite.MAJEUR, service.consulterMajorite(identifiant));
    }

    @Test
    void considersUserMinorBeforeEighteenthBirthday() {
        service.enregistrerDateNaissance(identifiant, LocalDate.of(2008, 6, 26));

        assertEquals(ProfilUtilisateur.StatutMajorite.MINEUR, service.consulterMajorite(identifiant));
    }

    @Test
    void rejectsFutureBirthDate() {
        LocalDate futureDate = LocalDate.of(2026, 6, 26);

        assertThrows(
            IllegalArgumentException.class,
            () -> service.enregistrerDateNaissance(identifiant, futureDate)
        );
    }

    @Test
    void returnsUnknownWhenNoBirthDateExists() {
        assertEquals(ProfilUtilisateur.StatutMajorite.INCONNU, service.consulterMajorite(identifiant));
    }

    private static class RepositoryMemoire implements ProfilUtilisateurRepository {

        private final Map<String, ProfilUtilisateur> profils = new HashMap<>();

        @Override
        public Optional<ProfilUtilisateur> findByIdentifiant(String identifiant) {
            return Optional.ofNullable(profils.get(identifiant));
        }

        @Override
        public ProfilUtilisateur save(ProfilUtilisateur profil) {
            profils.put(profil.identifiant(), profil);
            return profil;
        }
    }
}
