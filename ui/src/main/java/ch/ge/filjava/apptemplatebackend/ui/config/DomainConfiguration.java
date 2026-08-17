package ch.ge.filjava.apptemplatebackend.ui.config;

import ch.ge.filjava.apptemplatebackend.domain.profilutilisateur.ProfilUtilisateurRepository;
import ch.ge.filjava.apptemplatebackend.domain.profilutilisateur.ProfilUtilisateurService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class DomainConfiguration {

    @Bean
    public ProfilUtilisateurService profilUtilisateurService(
            ProfilUtilisateurRepository repository
    ) {
        return new ProfilUtilisateurService(
                repository,
                Clock.systemDefaultZone()
        );
    }
}