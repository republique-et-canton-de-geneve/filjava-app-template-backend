package ch.ge.filjava.apptemplatebackend.ui.rest.profilutilisateur;

import ch.ge.filjava.apptemplatebackend.domain.profilutilisateur.ProfilUtilisateur;
import ch.ge.filjava.apptemplatebackend.ui.rest.profilutilisateur.generated.dto.MajoriteResponse;
import ch.ge.filjava.apptemplatebackend.ui.rest.profilutilisateur.generated.dto.ProfilUtilisateurResponse;
import ch.ge.filjava.apptemplatebackend.ui.rest.profilutilisateur.generated.dto.StatutMajorite;
import org.springframework.stereotype.Component;

@Component
public final class ProfilUtilisateurDtoMapper {

    ProfilUtilisateurResponse toDto(ProfilUtilisateur profil) {
        return new ProfilUtilisateurResponse(profil.dateNaissance());
    }

    MajoriteResponse toDto(ProfilUtilisateur.StatutMajorite statut) {
        return new MajoriteResponse(StatutMajorite.fromValue(statut.name()));
    }
}
