package ch.ge.filjava.apptemplatebackend.ui.bdd.profilutilisateur;

import ch.ge.filjava.apptemplatebackend.domain.profilutilisateur.ProfilUtilisateur;
import ch.ge.filjava.apptemplatebackend.domain.profilutilisateur.ProfilUtilisateurRepository;
import ch.ge.filjava.apptemplatebackend.domain.profilutilisateur.ProfilUtilisateurService;
import ch.ge.filjava.apptemplatebackend.domain.security.UtilisateurCourantProvider;
import ch.ge.filjava.apptemplatebackend.ui.rest.profilutilisateur.ProfilUtilisateurDtoMapper;
import ch.ge.filjava.apptemplatebackend.ui.rest.profilutilisateur.ProfilUtilisateurResource;
import ch.ge.filjava.apptemplatebackend.ui.rest.profilutilisateur.generated.dto.DateNaissanceRequest;
import ch.ge.filjava.apptemplatebackend.ui.rest.profilutilisateur.generated.dto.MajoriteResponse;
import ch.ge.filjava.apptemplatebackend.ui.rest.profilutilisateur.generated.dto.StatutMajorite;
import io.cucumber.java.fr.Alors;
import io.cucumber.java.fr.Etantdonné;
import io.cucumber.java.fr.Quand;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProfilUtilisateurSteps {

    private final RepositoryMemoire repository = new RepositoryMemoire();
    private final UtilisateurCourantProvider utilisateurCourantProvider = () -> "user-123";

    private ProfilUtilisateurResource resource;
    private LocalDate dateNaissance;
    private MajoriteResponse response;

    @Etantdonné("nous sommes le {int} {word} {int}")
    public void dateReference(int jour, String mois, int annee) {
        LocalDate dateReference = parseDate(jour, mois, annee);
        Clock clock = Clock.fixed(
            dateReference.atTime(LocalTime.MIDNIGHT).toInstant(ZoneOffset.UTC),
            ZoneOffset.UTC
        );
        resource = new ProfilUtilisateurResource(
            new ProfilUtilisateurService(repository, clock),
            new ProfilUtilisateurDtoMapper(),
            utilisateurCourantProvider
        );
    }

    @Etantdonné("un utilisateur connecté né le {int} {word} {int}")
    public void utilisateurNeLe(int jour, String mois, int annee) {
        this.dateNaissance = parseDate(jour, mois, annee);
    }

    private LocalDate parseDate(int jour, String mois, int annee) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM uuuu", Locale.FRENCH);
        return LocalDate.parse("%d %s %d".formatted(jour, mois, annee), formatter);
    }

    @Quand("il enregistre sa date de naissance")
    public void enregistreDateNaissance() {
        resource.enregistrerDateNaissance(new DateNaissanceRequest(dateNaissance));
        response = resource.consulterMajorite();
    }

    @Alors("son statut de majorité est {word}")
    public void statutMajorite(String statut) {
        assertEquals(StatutMajorite.fromValue(statut), response.getStatut());
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
