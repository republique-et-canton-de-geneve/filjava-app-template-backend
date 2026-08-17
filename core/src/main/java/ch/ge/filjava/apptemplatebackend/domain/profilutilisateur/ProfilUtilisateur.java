package ch.ge.filjava.apptemplatebackend.domain.profilutilisateur;

import java.time.LocalDate;
import java.util.Objects;

public record ProfilUtilisateur(
    String identifiant,
    LocalDate dateNaissance
) {

    private static final int AGE_MAJORITE = 18;

    public ProfilUtilisateur {
        if (identifiant == null || identifiant.isBlank()) {
            throw new IllegalArgumentException("L'identifiant utilisateur est obligatoire");
        }
        Objects.requireNonNull(dateNaissance, "La date de naissance est obligatoire");
    }

    public ProfilUtilisateur(String identifiant, LocalDate dateNaissance, LocalDate dateReference) {
        this(identifiant, verifierDateNaissance(dateNaissance, dateReference));
    }

    public boolean estMajeur(LocalDate dateReference) {
        Objects.requireNonNull(dateReference, "La date de référence est obligatoire");
        return !dateNaissance.plusYears(AGE_MAJORITE).isAfter(dateReference);
    }

    private static LocalDate verifierDateNaissance(LocalDate dateNaissance, LocalDate dateReference) {
        Objects.requireNonNull(dateReference, "La date de référence est obligatoire");
        Objects.requireNonNull(dateNaissance, "La date de naissance est obligatoire");
        if (dateNaissance.isAfter(dateReference)) {
            throw new IllegalArgumentException("La date de naissance ne peut pas être future");
        }
        return dateNaissance;
    }

    public enum StatutMajorite {
        MAJEUR,
        MINEUR,
        INCONNU
    }
}
