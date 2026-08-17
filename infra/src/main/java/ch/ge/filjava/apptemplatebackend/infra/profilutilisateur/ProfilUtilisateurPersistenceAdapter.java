package ch.ge.filjava.apptemplatebackend.infra.profilutilisateur;

import ch.ge.filjava.apptemplatebackend.domain.profilutilisateur.ProfilUtilisateur;
import ch.ge.filjava.apptemplatebackend.domain.profilutilisateur.ProfilUtilisateurRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.Optional;

public class ProfilUtilisateurPersistenceAdapter implements ProfilUtilisateurRepository {

    private final EntityManager entityManager;

    public ProfilUtilisateurPersistenceAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<ProfilUtilisateur> findByIdentifiant(String identifiant) {
        return Optional.ofNullable(
            entityManager.find(ProfilUtilisateurEntity.class, identifiant)
        ).map(this::toDomain);
    }

    @Override
    @Transactional
    public ProfilUtilisateur save(ProfilUtilisateur profil) {
        ProfilUtilisateurEntity entity = new ProfilUtilisateurEntity(
            profil.identifiant(),
            profil.dateNaissance()
        );
        return toDomain(entityManager.merge(entity));
    }

    private ProfilUtilisateur toDomain(ProfilUtilisateurEntity entity) {
        return new ProfilUtilisateur(
            entity.getIdentifiantUtilisateur(),
            entity.getDateNaissance()
        );
    }
}
