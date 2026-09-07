package ch.ge.filjava.apptemplatebackend.ui.config;

import ch.ge.filjava.apptemplatebackend.ui.rest.profilutilisateur.ProfilUtilisateurResource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import jakarta.annotation.PostConstruct;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.lifecycle.SingletonResourceProvider;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CxfConfiguration {

    private final Bus bus;
    private final ProfilUtilisateurResource profilUtilisateurResource;
    private final ObjectMapper objectMapper;

    public CxfConfiguration(
            Bus bus,
            ProfilUtilisateurResource profilUtilisateurResource,
            ObjectMapper objectMapper
    ) {
        this.bus = bus;
        this.profilUtilisateurResource = profilUtilisateurResource;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void registerJaxRsServer() {
        JAXRSServerFactoryBean factory = new JAXRSServerFactoryBean();

        factory.setBus(bus);
        factory.setAddress("/");

        factory.setResourceClasses(ProfilUtilisateurResource.class);
        factory.setResourceProvider(
                ProfilUtilisateurResource.class,
                new SingletonResourceProvider(profilUtilisateurResource)
        );

        factory.setProviders(List.of(
                new JacksonJsonProvider(objectMapper)
        ));

        factory.create();
    }
}
