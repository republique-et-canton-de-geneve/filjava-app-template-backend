package ch.ge.filjava.apptemplatebackend.ui.config;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component("databaseStatus")
public class DatabaseStatusHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseStatusHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(1)
                    ? Health.up().build()
                    : Health.down().build();
        } catch (SQLException _) {
            return Health.down().build();
        }
    }
}