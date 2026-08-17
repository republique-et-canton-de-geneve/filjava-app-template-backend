package ch.ge.filjava.apptemplatebackend.domain.profilutilisateur;

import java.util.Optional;

public interface ProfilUtilisateurRepository {

    Optional<ProfilUtilisateur> findByIdentifiant(String identifiant);

    ProfilUtilisateur save(ProfilUtilisateur profil);
}
