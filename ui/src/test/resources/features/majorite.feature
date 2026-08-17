# language: fr
Fonctionnalité: Déterminer la majorité d'un utilisateur

  Scénario: Une personne ayant exactement 18 ans est majeure
    Étant donné que nous sommes le 25 juin 2026
    Et un utilisateur connecté né le 25 juin 2008
    Quand il enregistre sa date de naissance
    Alors son statut de majorité est MAJEUR

  Scénario: Une personne dont le dix-huitième anniversaire est demain est mineure
    Étant donné que nous sommes le 25 juin 2026
    Et un utilisateur connecté né le 26 juin 2008
    Quand il enregistre sa date de naissance
    Alors son statut de majorité est MINEUR
