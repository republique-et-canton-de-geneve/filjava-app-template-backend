package ch.ge.filjava.apptemplatebackend.infra.profilutilisateur;

import ch.ge.filjava.apptemplatebackend.domain.profilutilisateur.ProfilUtilisateur;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfilUtilisateurPersistenceAdapterTest {

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ProfilUtilisateurPersistenceAdapter adapter;


    @Test
    void shouldFindUtilisateurByIdentifiant() {
        String identifiant = "utilisateur-test";

        ProfilUtilisateurEntity entity = new ProfilUtilisateurEntity(
                identifiant,
                LocalDate.of(1990, 1, 1)
        );

        when(entityManager.find(
                ProfilUtilisateurEntity.class,
                identifiant
        )).thenReturn(entity);

        Optional<ProfilUtilisateur> result = adapter.findByIdentifiant(identifiant);

        assertTrue(result.isPresent());
        assertEquals(identifiant, result.get().identifiant());
        assertEquals(LocalDate.of(1990, 1, 1), result.get().dateNaissance());
        verify(entityManager)
                .find(ProfilUtilisateurEntity.class, identifiant);
    }


    @Test
    void shouldReturnEmptyWhenUtilisateurDoesNotExist() {
        String identifiant = "inexistant";
        when(entityManager.find(
                ProfilUtilisateurEntity.class,
                identifiant
        )).thenReturn(null);

        Optional<ProfilUtilisateur> result = adapter.findByIdentifiant(identifiant);

        assertFalse(result.isPresent());
        verify(entityManager)
                .find(ProfilUtilisateurEntity.class, identifiant);
    }


    @Test
    void shouldSaveUtilisateur() {
        ProfilUtilisateur profil = new ProfilUtilisateur(
                "utilisateur-test",
                LocalDate.of(1990, 1, 1)
        );

        ProfilUtilisateurEntity savedEntity = new ProfilUtilisateurEntity(
                "utilisateur-test",
                LocalDate.of(1990, 1, 1)
        );

        when(entityManager.merge(any(ProfilUtilisateurEntity.class)))
                .thenReturn(savedEntity);

        ProfilUtilisateur result = adapter.save(profil);

        assertEquals("utilisateur-test", result.identifiant());
        assertEquals(LocalDate.of(1990, 1, 1), result.dateNaissance());
        verify(entityManager)
                .merge(any(ProfilUtilisateurEntity.class));
    }
}