package ch.ge.filjava.apptemplatebackend.ui.security;

import com.hazelcast.config.Config;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionClusterConfigurationTest {

    @Test
    void installeLeFiltreDeSessionSpring() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources().addFirst(
                    new MapPropertySource("application", Map.of("spring.application.name", UUID.randomUUID().toString()))
            );
            context.register(SessionClusterConfiguration.class);
            context.refresh();

            assertNotNull(context.getBean("springSessionRepositoryFilter"), "Le filtre Spring Session doit être installé");
            Config config = context.getBean(Config.class);
            assertFalse(config.getNetworkConfig().getJoin().getMulticastConfig().isEnabled(),
                    "Le multicast doit être désactivé");
            assertFalse(config.getNetworkConfig().getJoin().getAutoDetectionConfig().isEnabled(),
                    "La détection automatique doit être désactivée");
        }
    }

    @Test
    void partageEtSupprimeUneSessionEntreDeuxMembres() throws IOException, InterruptedException {
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
            CountDownLatch secondMemberJoined = new CountDownLatch(1);
            first.getCluster().addMembershipListener(new MembershipListener() {
                @Override
                public void memberAdded(MembershipEvent event) {
                    secondMemberJoined.countDown();
                }

                @Override
                public void memberRemoved(MembershipEvent event) {
                    // Aucun événement attendu pendant l'établissement du cluster.
                }
            });
            HazelcastInstance second = Hazelcast.newHazelcastInstance(secondConfig);
            try {
                assertTrue(secondMemberJoined.await(20, TimeUnit.SECONDS),
                        "Le second membre doit rejoindre le cluster dans les 20 secondes");

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
                    assertNotNull(received, "La session créée sur le premier membre doit être visible sur le second");
                    assertEquals("partage", received.getAttribute("message"),
                            "Les attributs de session doivent être partagés entre les membres");
                    SecurityContextImpl receivedContext = received.getAttribute(
                            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
                    assertEquals("utilisateur", receivedContext.getAuthentication().getName(),
                            "Le contexte de sécurité doit être partagé entre les membres");

                    secondSessions.deleteById(session.getId());
                    assertNull(firstSessions.findById(session.getId()),
                            "La suppression de la session doit être visible sur le premier membre");
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
