# Intégration du template dans Backstage

Ce dossier contient l'action personnalisée appelée par `template.yaml` et les
outils qui permettent de tester la génération sans publier de dépôt.

## À installer dans le backend Backstage

Le dossier `scaffolder-backend-module-filjava` est un module backend Backstage.
L'administrateur doit l'ajouter au workspace de son instance, installer ses
dépendances avec la version déjà utilisée par cette instance, puis enregistrer
le module dans `packages/backend/src/index.ts` :

```ts
backend.add(import('@internal/backstage-plugin-scaffolder-backend-module-filjava'));
```

L'action publiée porte l'identifiant :

```text
filjava:javaPackage:rename
```

Elle doit ensuite apparaître dans `/create/actions`.

## Points à confirmer avec l'administrateur

- la version exacte de Backstage et son système de backend ;
- l'hôte déclaré pour l'intégration GitLab ;
- la disponibilité du module `@backstage/plugin-scaffolder-backend-module-gitlab`
  et de l'action `publish:gitlab` ;
- la méthode d'enregistrement de `template.yaml` dans le catalogue ;
- le groupe propriétaire du template ;
- les conventions relatives à `owner`, `system`, `lifecycle` et aux annotations ;
- les permissions de création de dépôts et d'enregistrement de composants ;
- la convention de destination des images de conteneur.

Le champ `allowedHosts` du `RepoUrlPicker` n'est volontairement pas renseigné
tant que le hostname GitLab n'a pas été confirmé. De même, aucune annotation
propre à l'organisation n'est inventée dans `catalog-info.yaml`.

## Test local

Depuis la racine du dépôt :

```shell
npm test --prefix backstage/scaffolder-backend-module-filjava
node backstage/scripts/test-template.mjs
```

Le second script produit un projet temporaire, contrôle son contenu et tente
également `mvn clean verify` lorsque Maven est disponible.

Par défaut, le projet généré est supprimé à la fin du test. Pour le conserver et
l'inspecter, définir la variable suivante avant de lancer le script :

```powershell
$env:KEEP_GENERATED_PROJECT = 'true'
node backstage/scripts/test-template.mjs
```

Le chemin du projet est affiché à la fin de l'exécution, par exemple :

```text
Projet généré et contrôlé dans C:\Users\utilisateur\AppData\Local\Temp\filjava-generated-abc123
```

Ce répertoire peut être ouvert dans l'Explorateur Windows ou importé dans
IntelliJ IDEA à partir de son `pom.xml` racine. Il contient les packages Java
renommés, la nouvelle classe principale, `catalog-info.yaml` et les rapports de
test Maven dans les répertoires `target`.

Pour rétablir la suppression automatique dans la session PowerShell :

```powershell
Remove-Item Env:KEEP_GENERATED_PROJECT
```

Un projet conservé n'est plus supprimé automatiquement et doit être effacé
manuellement après inspection.
