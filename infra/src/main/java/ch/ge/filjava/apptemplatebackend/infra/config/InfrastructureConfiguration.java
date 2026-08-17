package ch.ge.filjava.apptemplatebackend.infra.config;

import ch.ge.filjava.apptemplatebackend.domain.profilutilisateur.ProfilUtilisateurRepository;
import ch.ge.filjava.apptemplatebackend.infra.profilutilisateur.ProfilUtilisateurPersistenceAdapter;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfrastructureConfiguration {

    @Bean
    public ProfilUtilisateurRepository profilUtilisateurRepository(EntityManager entityManager) {
        return new ProfilUtilisateurPersistenceAdapter(entityManager);
    }
}
