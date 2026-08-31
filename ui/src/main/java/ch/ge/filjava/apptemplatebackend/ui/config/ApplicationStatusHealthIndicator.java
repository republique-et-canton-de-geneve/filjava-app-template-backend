package ch.ge.filjava.apptemplatebackend.ui.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("applicationStatus")
public class ApplicationStatusHealthIndicator implements HealthIndicator {

    private final String version;

    public ApplicationStatusHealthIndicator(
            @Value("${info.app.version}") String version) {
        this.version = version;
    }

    @Override
    public Health health() {
        return Health.up()
                .withDetail("version", version)
                .build();
    }
}