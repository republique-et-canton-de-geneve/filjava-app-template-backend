package ch.ge.filjava.apptemplatebackend.ui.security;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationResource {

    @GetMapping("/session")
    SessionResponse session(Authentication authentication) {
        return new SessionResponse(authentication != null && authentication.isAuthenticated());
    }

    @GetMapping("/csrf")
    CsrfResponse csrf(CsrfToken csrfToken) {
        return new CsrfResponse(csrfToken.getToken());
    }

    @PostMapping("/logout")
    void logout(HttpSession session) {
        session.invalidate();
    }

    record SessionResponse(boolean authenticated) {
    }

    record CsrfResponse(String token) {
    }
}
