package ch.ge.filjava.apptemplatebackend.infra.profilutilisateur;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "profil_utilisateur")
public class ProfilUtilisateurEntity {

    @Id
    @Column(name = "identifiant_utilisateur", nullable = false)
    private String identifiantUtilisateur;

    @Column(name = "date_naissance", nullable = false)
    private LocalDate dateNaissance;

    protected ProfilUtilisateurEntity() {
    }

    public ProfilUtilisateurEntity(String identifiantUtilisateur, LocalDate dateNaissance) {
        this.identifiantUtilisateur = identifiantUtilisateur;
        this.dateNaissance = dateNaissance;
    }

    public String getIdentifiantUtilisateur() {
        return identifiantUtilisateur;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }
}
