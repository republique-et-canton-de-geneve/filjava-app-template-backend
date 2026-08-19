CREATE TABLE profil_utilisateur (
    identifiant_utilisateur VARCHAR(255) NOT NULL,
    date_naissance DATE NOT NULL,
    CONSTRAINT pk_profil_utilisateur
        PRIMARY KEY (identifiant_utilisateur)
);