package ch.ge.filjava.apptemplatebackend.ui.rest.accueil;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.springframework.stereotype.Component;

@Component
@Path("/accueil")
@Produces("text/html; charset=UTF-8")
public class AccueilResource {

    @GET
    public String accueillir() {
        return """
                <!doctype html>
                <html lang="fr">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>Application template Java</title>
                    <style>
                        :root {
                            color-scheme: light;
                            font-family: Inter, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                            color: #172033;
                            background: #eef2f7;
                        }
                        * { box-sizing: border-box; }
                        body {
                            min-height: 100vh;
                            margin: 0;
                            display: grid;
                            place-items: center;
                            padding: 2rem;
                            background: linear-gradient(135deg, #eef2f7 0%, #dfe9f3 100%);
                        }
                        main {
                            width: min(42rem, 100%);
                            padding: 3rem;
                            border-radius: 1.25rem;
                            background: rgba(255, 255, 255, 0.94);
                            box-shadow: 0 1.5rem 4rem rgba(23, 32, 51, 0.14);
                            text-align: center;
                        }
                        .badge {
                            display: inline-block;
                            margin-bottom: 1.25rem;
                            padding: .45rem .85rem;
                            border-radius: 999px;
                            color: #075985;
                            background: #e0f2fe;
                            font-size: .8rem;
                            font-weight: 700;
                            letter-spacing: .08em;
                            text-transform: uppercase;
                        }
                        h1 {
                            margin: 0;
                            font-size: clamp(2rem, 6vw, 3.25rem);
                            line-height: 1.08;
                            letter-spacing: -.04em;
                        }
                        p {
                            margin: 1.25rem auto 0;
                            max-width: 32rem;
                            color: #526078;
                            font-size: 1.1rem;
                            line-height: 1.7;
                        }
                        strong { color: #0369a1; }
                    </style>
                </head>
                <body>
                    <main>
                        <span class="badge">Java 25 · Backend</span>
                        <h1>Bienvenue</h1>
                        <p>Bienvenue dans l'application template de la <strong>filière Java</strong>.</p>
                    </main>
                </body>
                </html>
                """;
    }
}
