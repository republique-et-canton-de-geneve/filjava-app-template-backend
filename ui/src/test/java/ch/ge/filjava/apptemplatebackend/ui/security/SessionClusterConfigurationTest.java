package ch.ge.filjava.apptemplatebackend.ui.security;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.session.HazelcastIndexedSessionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class SessionClusterConfigurationTest {

    @Test
    void installeLeFiltreDeSessionSpring() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(
                    new MapPropertySource("application", Map.of("spring.application.name", UUID.randomUUID().toString()))
            );
            context.register(SessionClusterConfiguration.class);
            context.refresh();

            assertNotNull(context.getBean("springSessionRepositoryFilter"));
            Config config = context.getBean(Config.class);
            assertFalse(config.getNetworkConfig().getJoin().getMulticastConfig().isEnabled());
            assertFalse(config.getNetworkConfig().getJoin().getAutoDetectionConfig().isEnabled());
        }
    }

    @Test
    void partageEtSupprimeUneSessionEntreDeuxMembres() throws IOException {
        SessionClusterConfiguration configuration = new SessionClusterConfiguration();
        String clusterName = UUID.randomUUID().toString();
        int firstPort;
        try (ServerSocket socket = new ServerSocket(0, 50, InetAddress.getLoopbackAddress())) {
            firstPort = socket.getLocalPort();
        }

        Config firstConfig = configuration.localHazelcastConfig(clusterName);
        firstConfig.getNetworkConfig().setPort(firstPort).setPortAutoIncrement(false);
        firstConfig.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true);

        Config secondConfig = configuration.localHazelcastConfig(clusterName);
        secondConfig.getNetworkConfig().setPort(0);
        secondConfig.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(true)
                .addMember(InetAddress.getLoopbackAddress().getHostAddress() + ":" + firstPort);

        HazelcastInstance first = Hazelcast.newHazelcastInstance(firstConfig);
        try {
            HazelcastInstance second = Hazelcast.newHazelcastInstance(secondConfig);
            try {
                assertTimeoutPreemptively(Duration.ofSeconds(20), () -> {
                    while (first.getCluster().getMembers().size() != 2) {
                        Thread.sleep(100);
                    }
                });

                HazelcastIndexedSessionRepository firstSessions = new HazelcastIndexedSessionRepository(first);
                HazelcastIndexedSessionRepository secondSessions = new HazelcastIndexedSessionRepository(second);
                firstSessions.afterPropertiesSet();
                secondSessions.afterPropertiesSet();
                try {
                    var session = firstSessions.createSession();
                    session.setAttribute("message", "partage");
                    var securityContext = new SecurityContextImpl();
                    securityContext.setAuthentication(new TestingAuthenticationToken("utilisateur", null, "ROLE_USER"));
                    session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
                    firstSessions.save(session);

                    var received = secondSessions.findById(session.getId());
                    assertNotNull(received);
                    assertEquals("partage", received.getAttribute("message"));
                    SecurityContextImpl receivedContext = received.getAttribute(
                            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
                    assertEquals("utilisateur", receivedContext.getAuthentication().getName());

                    secondSessions.deleteById(session.getId());
                    assertNull(firstSessions.findById(session.getId()));
                } finally {
                    secondSessions.destroy();
                    firstSessions.destroy();
                }
            } finally {
                second.shutdown();
            }
        } finally {
            first.shutdown();
        }
    }
}
