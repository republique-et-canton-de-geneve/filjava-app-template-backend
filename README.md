# FILJAVA - Template backend

Ce projet est un template Java 25 à destination des personnes souhaitant initier
un projet Java sur une base minimale fondée sur une architecture hexagonale.

Le modèle d'exemple représente une tranche de la future V1 sans l'implémenter :
un utilisateur authentifié peut enregistrer sa date de naissance et consulter
son statut de majorité.

Le compte et les informations personnelles restent la responsabilité du VLDAP.
L'application conserve uniquement l'identifiant technique de l'utilisateur et
sa date de naissance.

## Modules

Le projet est organisé en trois modules Maven :
- `core` contient le domaine, les services métier et les ports.
- `infra` contient les adaptateurs techniques qui implémentent les ports.
- `ui` contient les interfaces d'entrée de l'application et les scénarios BDD.

Les dépendances vont vers le domaine : `core` ne dépend ni de `infra`, ni de
`ui`.

## Organisation du code

Le code est d'abord séparé par couche, puis regroupé par fonctionnalité. Les
fichiers d'une même fonctionnalité restent ainsi proches les uns des autres.

```text
core/
└── .../domain/
    └── profilutilisateur/
    │   ├── ProfilUtilisateur.java
    │   ├── ProfilUtilisateurRepository.java
    │   └── ProfilUtilisateurService.java
    └── security/
        ├── UtilisateurCourantProvider.java

infra/
└── .../infra/
    ├── config/
    │   └── InfrastructureConfiguration.java
    └── profilutilisateur/
        ├── ProfilUtilisateurEntity.java
        └── ProfilUtilisateurPersistenceAdapter.java

ui/
└── .../ui/
    ├── main/
    │   └── FiljavaApplication.java
    ├── config/
    │   ├── CxfConfiguration.java
    │   ├── JacksonConfiguration.java
    │   └── DomainConfiguration.java
    ├── security/
    │   └── SecurityUtilisateurCourantProvider.java
    ├── rest/
    │   └── profilutilisateur/
    │       ├── ProfilUtilisateurResource.java
    │       ├── ProfilUtilisateurDtoMapper.java
    │       └── generated/
    │           ├── api/
    │           └── dto/
    └── bdd/
        └── profilutilisateur/
            └── ProfilUtilisateurSteps.java
```

Dans `ui`, le premier niveau représente le type d'interface (`rest`,
`messaging`, `batch`, etc.). Le niveau suivant représente une fonctionnalité ou
une ressource API, pas nécessairement une entité persistée.

L'exposition des ressources REST est réalisée avec Apache CXF via JAX-RS.
Les ressources REST sont déclarées dans le module `ui` et publiées par la
configuration `CxfConfiguration`.

Les tests comportementaux (Cucumber) sont situés dans le package `bdd` et
contiennent les étapes de test (`ProfilUtilisateurSteps.java`).

Le template repose sur une **architecture hexagonale**. Chaque nouvelle
fonctionnalité doit être organisée par domaine fonctionnel dans chacune
des couches concernées (`core`, `infra` et `ui`). Une organisation globale
par type technique (`model`, `repository`, `service`, `request`, `response`)
est volontairement évitée.

## Infrastructure

Les adaptateurs d'infrastructure implémentent les ports définis dans le domaine.
Ils peuvent utiliser les technologies de persistance ou d'intégration nécessaires.

Dans ce template, la persistance du profil utilisateur est réalisée avec JPA via
un adaptateur implémentant le port défini dans le domaine.

La couche `infra` reste indépendante des détails d'exposition REST présents dans
la couche `ui`.

## Modèle d'exemple

La règle de majorité est évaluée à la date courante :

- 18 ans révolus : `MAJEUR`
- moins de 18 ans : `MINEUR`
- aucune date enregistrée : `INCONNU`

Les points d'entrée illustratifs sont :

```text
PUT /services/profil/date-naissance
GET /services/profil/majorite
```

Le préfixe `/services` correspond au chemin d'exposition Apache CXF.
Les chemins fonctionnels des ressources restent définis dans les classes JAX-RS.

L'identifiant utilisateur est fourni par le port `UtilisateurCourantProvider`
défini dans le domaine.

L'application est un serveur de ressources OAuth2. Les routes métier
`/services/**` nécessitent un Bearer token JWT valide. Le sujet (`sub`) du token
fournit l'identifiant technique de l'utilisateur au domaine via le port
`UtilisateurCourantProvider`.

## Contrat OpenAPI

Le contrat REST versionné se trouve dans
`ui/src/main/resources/openapi/profil-utilisateur-v1.yaml`. Le module `ui` génère
automatiquement l'interface API et les DTO JAX-RS/Jakarta pendant
`generate-sources` avec `openapi-generator-maven-plugin`.

La ressource REST implémente l'interface générée et mappe explicitement les DTO
vers le domaine via `ProfilUtilisateurDtoMapper`.

Le contrat est également publié sous forme d'interface Swagger UI
par GitLab Pages à chaque pipeline de la branche par défaut. L'URL du
déploiement est aussi disponible dans **Déploiement > Pages** dans GitLab.
Cette interface est volontairement en lecture seule : les appels à l'API
restent à effectuer sur une instance lancée de l'application. Tous les fichiers
`.yaml` et `.yml` placés directement dans `ui/src/main/resources/openapi` sont
publiés et ajoutés automatiquement au sélecteur de contrats de Swagger UI.

## Prérequis

Le template nécessite :

- Java 25
- Maven 3.9 ou supérieur
- Git
- Un accès à une base de données PostgreSQL

L'utilisation d'IntelliJ IDEA est recommandée.

## Créer une nouvelle application à partir du template

Tant que le template n'est pas intégré dans Backstage, une application peut
être générée localement avec la commande paramétrable documentée dans
[`backstage/README.md`](backstage/README.md#générer-une-application-sans-backstage).

Le fichier `template.yaml` décrit un Software Template Backstage. Depuis la page
**Create**, renseigner :

- l'identifiant technique en minuscules et tirets, par exemple
  `gestion-dossiers` ;
- le nom lisible et la description de l'application ;
- un package Java racine en minuscules, par exemple `ch.ge.gestiondossiers` ;
- l'équipe propriétaire et le système sélectionnés dans le catalogue ;
- la destination du dépôt GitLab.

Backstage adapte automatiquement les coordonnées Maven, le nom Spring, les
métadonnées, les packages Java et la classe principale. Pour
`gestion-dossiers`, la classe principale devient
`GestionDossiersApplication`. Il crée ensuite le dépôt et enregistre le fichier
`catalog-info.yaml` généré dans le Software Catalog.

Les règles de transformation, le test local et les informations nécessaires à
l'installation de l'action personnalisée sont documentés dans
`backstage/README.md`.

Pour tester localement le rendu avant son intégration à Backstage :

```shell
npm test --prefix backstage/scaffolder-backend-module-filjava
node backstage/scripts/test-template.mjs
```

Le test de génération utilise un répertoire temporaire, vérifie qu'aucun ancien
identifiant ou marqueur Backstage ne subsiste et exécute `mvn clean verify` si
Maven est disponible.

La procédure complète, notamment la conservation et l'inspection du projet
généré, est disponible dans [`backstage/README.md`](backstage/README.md#test-local).

## Vérification

Pour compiler le projet et exécuter l'ensemble des tests :

```shell
mvn clean verify
```

Cette commande compile les trois modules, exécute les tests unitaires du domaine
et les scénarios Cucumber majeur/mineur.

## Lancement de l'application

Pour démarrer l'application localement :

```shell
cd ui
mvn spring-boot:run
```

L'application utilise une base de données PostgreSQL configurée via la datasource
Spring Boot et expose les ressources REST via Apache CXF.

Elle expose également des endpoints techniques Spring Boot Actuator permettant
le suivi de l'état de l'application.

La validation des jetons nécessite l'accès au fournisseur OIDC configuré.

## Configuration

L'application utilise une base de données PostgreSQL externe.

Avant de démarrer l'application, définir les variables d'environnement suivantes :

```text
GINA_ISSUER_URI=https://***
DB_HOST=***
DB_NAME=***
DB_USERNAME=***
DB_PASSWORD=***
```

Les valeurs sont propres à l'environnement et ne doivent pas être versionnées.

Aucun client secret n'est nécessaire :
le backend valide des Bearer tokens et n'initie pas de connexion OIDC.

La configuration technique de l'application est centralisée dans :
ui/src/main/resources/application.yml

Elle contient notamment :
- La configuration de la datasource PostgreSQL
- La configuration JPA/Hibernate
- La configuration du serveur REST Apache CXF
- La configuration des endpoints techniques Spring Boot Actuator
- Les paramètres nécessaires au démarrage local

Le raccordement à un annuaire utilisateur (VLDAP) ne fait pas partie de ce squelette.

La supervision applicative est préparée via Spring Boot Actuator avec les
endpoints techniques de santé (`health`) et d'information (`info`).
La configuration complète des métriques et des outils de supervision reste
dépendante de l'environnement de déploiement.

## Supervision

L'application utilise Spring Boot Actuator afin d'exposer des endpoints techniques
permettant de vérifier son état, fournir des informations générales et exposer
des métriques applicatives.

Les endpoints actuellement exposés sont :

```text
GET /actuator/health
GET /actuator/info
GET /actuator/prometheus
```

Le endpoint `health` permet de vérifier l'état général de l'application.

La disponibilité de la base de données PostgreSQL est prise en compte dans
l'état de readiness. En cas d'indisponibilité de la base de données,
l'application reste vivante mais n'est plus considérée comme prête à recevoir
du trafic.

Des endpoints dédiés aux probes de disponibilité sont également exposés afin
de permettre leur utilisation par une plateforme d'orchestration telle
qu'OpenShift :

```text
GET /actuator/health/liveness
GET /actuator/health/readiness
```

Le endpoint `info` fournit les informations générales de l'application définies
dans la configuration.

Le endpoint `prometheus` expose les métriques au format attendu par Prometheus.

Les autres endpoints Actuator ne sont volontairement pas exposés par défaut.
Ils pourront être activés selon les besoins de l'environnement de déploiement.

## Documentation

Une documentation complémentaire est disponible dans le Wiki du projet.

Elle décrit notamment :

- Les règles de construction applicative
- Les règles de versionnement et de déploiement
- Les standards de configuration et de déploiement de la filière Java
