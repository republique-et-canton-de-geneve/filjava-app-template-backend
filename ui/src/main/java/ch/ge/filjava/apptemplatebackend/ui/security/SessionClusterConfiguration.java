package ch.ge.filjava.apptemplatebackend.ui.security;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.session.HazelcastSessionConfiguration;
import com.hazelcast.spring.session.config.annotation.web.http.EnableHazelcastHttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableHazelcastHttpSession
public class SessionClusterConfiguration {

    @Bean(destroyMethod = "shutdown")
    HazelcastInstance hazelcastInstance(Config config) {
        return Hazelcast.newHazelcastInstance(config);
    }

    @Bean
    @Profile("!prod")
    Config localHazelcastConfig(@Value("${spring.application.name}") String clusterName) {
        Config config = baseConfig(clusterName);
        config.getNetworkConfig().setPortAutoIncrement(true);
        return config;
    }

    @Bean
    @Profile("prod")
    Config openshiftHazelcastConfig(
            @Value("${spring.application.name}") String clusterName,
            @Value("${HAZELCAST_SERVICE_DNS}") String serviceDns
    ) {
        if (serviceDns.isBlank()) {
            throw new IllegalArgumentException("HAZELCAST_SERVICE_DNS doit être renseigné en production");
        }
        Config config = baseConfig(clusterName);
        config.getNetworkConfig().setPort(5701).setPortAutoIncrement(false);
        config.getNetworkConfig().getJoin().getKubernetesConfig().setEnabled(true)
                .setProperty("service-dns", serviceDns);
        return config;
    }

    private Config baseConfig(String clusterName) {
        Config config = new Config().setClusterName(clusterName);
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
        config.getNetworkConfig().getJoin().getAutoDetectionConfig().setEnabled(false);
        HazelcastSessionConfiguration.applySerializationConfig(config);
        return config;
    }
}
