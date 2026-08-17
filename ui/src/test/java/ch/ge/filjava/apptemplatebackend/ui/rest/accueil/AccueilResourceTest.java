package ch.ge.filjava.apptemplatebackend.ui.rest.accueil;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccueilResourceTest {

    private final AccueilResource accueilResource = new AccueilResource();

    @Test
    void devraitRetournerUnePageAccueilEnFrancais() {
        String page = accueilResource.accueillir();

        assertAll(
                () -> assertTrue(page.contains("<meta charset=\"UTF-8\">")),
                () -> assertTrue(page.contains("<h1>Bienvenue</h1>")),
                () -> assertTrue(page.contains("filière Java"))
        );
    }

    @Test
    void devraitExposerAccueilAvecJaxRs() {
        assertAll(
                () -> assertEquals("/accueil", AccueilResource.class.getAnnotation(Path.class).value()),
                () -> assertNotNull(AccueilResource.class.getMethod("accueillir").getAnnotation(GET.class))
        );
    }
}
