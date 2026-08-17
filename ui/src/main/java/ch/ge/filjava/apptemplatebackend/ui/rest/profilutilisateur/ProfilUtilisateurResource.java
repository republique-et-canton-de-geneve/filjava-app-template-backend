package ch.ge.filjava.apptemplatebackend.ui.rest.profilutilisateur;

import ch.ge.filjava.apptemplatebackend.domain.profilutilisateur.ProfilUtilisateur;
import ch.ge.filjava.apptemplatebackend.domain.profilutilisateur.ProfilUtilisateurService;
import ch.ge.filjava.apptemplatebackend.domain.security.UtilisateurCourantProvider;
import ch.ge.filjava.apptemplatebackend.ui.rest.profilutilisateur.generated.api.ProfilUtilisateurApi;
import ch.ge.filjava.apptemplatebackend.ui.rest.profilutilisateur.generated.dto.DateNaissanceRequest;
import ch.ge.filjava.apptemplatebackend.ui.rest.profilutilisateur.generated.dto.MajoriteResponse;
import ch.ge.filjava.apptemplatebackend.ui.rest.profilutilisateur.generated.dto.ProfilUtilisateurResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Path("/profil")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProfilUtilisateurResource implements ProfilUtilisateurApi {

    private final ProfilUtilisateurService profilUtilisateurService;
    private final ProfilUtilisateurDtoMapper mapper;
    private final UtilisateurCourantProvider utilisateurCourantProvider;

    public ProfilUtilisateurResource(ProfilUtilisateurService profilUtilisateurService,
                                     ProfilUtilisateurDtoMapper mapper,
                                     UtilisateurCourantProvider utilisateurCourantProvider) {
        this.profilUtilisateurService = Objects.requireNonNull(profilUtilisateurService);
        this.mapper = Objects.requireNonNull(mapper);
        this.utilisateurCourantProvider = Objects.requireNonNull(utilisateurCourantProvider);
    }

    @Override
    @PUT
    @Path("/date-naissance")
    public ProfilUtilisateurResponse enregistrerDateNaissance(DateNaissanceRequest request) {
        Objects.requireNonNull(request, "Le formulaire est obligatoire");
        ProfilUtilisateur profil = profilUtilisateurService.enregistrerDateNaissance(
            utilisateurCourantProvider.getIdentifiant(),
                request.getDateNaissance()
        );
        return mapper.toDto(profil);
    }

    @Override
    @GET
    @Path("/majorite")
    public MajoriteResponse consulterMajorite() {
        return mapper.toDto(profilUtilisateurService.consulterMajorite(utilisateurCourantProvider.getIdentifiant()));
    }
}
