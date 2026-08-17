package ch.ge.filjava.apptemplatebackend.domain.profilutilisateur;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

import static ch.ge.filjava.apptemplatebackend.domain.profilutilisateur.ProfilUtilisateur.StatutMajorite.INCONNU;
import static ch.ge.filjava.apptemplatebackend.domain.profilutilisateur.ProfilUtilisateur.StatutMajorite.MAJEUR;
import static ch.ge.filjava.apptemplatebackend.domain.profilutilisateur.ProfilUtilisateur.StatutMajorite.MINEUR;

public class ProfilUtilisateurService {

    private final ProfilUtilisateurRepository repository;
    private final Clock clock;

    public ProfilUtilisateurService(ProfilUtilisateurRepository repository, Clock clock) {
        this.repository = Objects.requireNonNull(repository);
        this.clock = Objects.requireNonNull(clock);
    }

    public ProfilUtilisateur enregistrerDateNaissance(
        String identifiant,
        LocalDate dateNaissance
    ) {
        return repository.save(new ProfilUtilisateur(identifiant, dateNaissance, LocalDate.now(clock)));
    }

    public ProfilUtilisateur.StatutMajorite consulterMajorite(String identifiant) {
        return repository.findByIdentifiant(identifiant)
            .map(profil -> profil.estMajeur(LocalDate.now(clock))
                ? MAJEUR
                : MINEUR)
            .orElse(INCONNU);
    }
}
