# Coupe du Monde 2022 - Application Java Swing

Application bureau Java Swing connectée à une base de données MySQL, permettant de gérer les équipes, les stades, la planification des matchs, la saisie des scores et le calcul dynamique des classements.

**Équipe** : Arsène (Lead / Intégration), Mohamine, Martin
**Deadline** : Aujourd'hui à 00h00
**Stack** : Java (JDK 8+), Java Swing, MySQL, JDBC (mysql-connector-java.jar), Git / GitHub

---

## 1. État d'avancement

L'architecture de base est en place sur la branche `main` :

- Dépôt GitHub créé et `.gitignore` configuré.
- Script SQL complet de création des tables (`pays`, `stades`, `matchs`) et données de test (`database.sql`).
- Pilote JDBC MySQL ajouté dans le dossier `lib/`.
- Classe de connexion centralisée (`src/db/ConnexionDB.java`).
- Fenêtre principale à onglets (`src/ui/MainFrame.java`) reliée aux 3 modules.
- Squelettes des 3 panneaux individuels (`PanelStadeEtPays`, `PanelMatchs`, `PanelClassement`).

---

## 2. Guide de démarrage

### Étape 1 - Récupérer le projet et créer sa branche

```
git clone <URL_DU_DEPOT_GITHUB>
cd <NOM_DU_DOSSIER>
```

Créer sa branche personnelle (ne pas coder sur `main`) :

```
git checkout -b feat/mohamine-matchs
git checkout -b feat/martin-classement
git checkout -b feat/arsene-stades
```

### Étape 2 - Configurer la base de données MySQL

1. Ouvrir MySQL (Workbench, phpMyAdmin ou ligne de commande) et créer la base `coupe_du_monde`.
2. Exécuter le contenu de `database.sql` sur cette base.
3. Vérifier les identifiants dans `src/db/ConnexionDB.java` (configuration partagée : utilisateur `user_cdm`, mot de passe `cdm2022`). Ne pas pousser de mot de passe personnel modifié.

### Étape 3 - Lier le pilote MySQL (`lib/*.jar`)

- VS Code : panneau `JAVA PROJECTS` > `Referenced Libraries` > `+` > sélectionner le `.jar` dans `lib/`.
- IntelliJ : clic droit sur le `.jar` dans `lib/` > `Add as Library...`.
- Eclipse : clic droit sur le `.jar` > `Build Path` > `Add to Build Path`.

### Étape 4 - Tester le lancement

Exécuter `src/ui/MainFrame.java`. Une fenêtre avec 3 onglets doit s'ouvrir sans erreur.

---

## 3. Répartition des tâches et cahier des charges

Règle d'or anti-conflit : chacun travaille exclusivement dans son propre dossier `src/ui/<prenom>/`.

### Arsène - Équipes et Stades (Questions 1 et 2)

Fichier : `src/ui/arsene/PanelStadeEtPays.java`

- Instanciation des 8 équipes (Q1) : bouton ou méthode au démarrage insérant les 8 équipes de 2022 (Qatar, Équateur, Sénégal, Pays-Bas en groupe A ; Angleterre, Iran, États-Unis, Pays de Galles en groupe B) avec `ON CONFLICT DO NOTHING` (ou `INSERT IGNORE` en MySQL).
- Ajout de stade (Q2) : formulaire (pays, ville, nom_stade) et bouton Enregistrer insérant dans la table `stades`.
- Affichage : `JTable` listant les stades enregistrés, mise à jour automatique après chaque ajout.

### Mohamine - Planification et Scores (Question 3)

Fichier : `src/ui/mohamine/PanelMatchs.java`

- Planifier un match : deux `JComboBox` pour `nom_pays_1` et `nom_pays_2` chargées depuis `pays`, une `JComboBox` pour le stade (`stades`), et un champ date (format `YYYY-MM-DD` via `java.sql.Date.valueOf()`).
- Validation métier : empêcher un pays de jouer contre lui-même (`nom_pays_1` égal à `nom_pays_2`). Les buts restent à `NULL` lors de la planification.
- Saisir les scores : `JTable` affichant tous les matchs. Permettre de sélectionner un match et d'enregistrer ou modifier `pays_1_buts` et `pays_2_buts` (`UPDATE matchs SET ... WHERE id_match = ?`).

### Martin - Moteur de Classement (Question 4)

Fichier : `src/ui/martin/PanelClassement.java`

- Astuce : insérer 2 ou 3 faux matchs avec scores dans MySQL pour tester les calculs sans attendre Mohamine.
- Calcul des statistiques : récupérer tous les pays (`pays`) et les matchs joués (`WHERE pays_1_buts IS NOT NULL AND pays_2_buts IS NOT NULL`). Calculer pour chaque équipe : MJ, G, N, P, BP, BC, Diff (BP - BC), Points (G*3 + N*1).
- Tri : par points décroissants, puis différence de buts décroissante.
- Interface : sélecteur (Groupe A, Groupe B, Classement Général), `JTable` et bouton Actualiser pour recharger les données.

---

## 4. Intégration finale et livraison

### 23h15 - Envoi du code sur GitHub

```
git add src/ui/<votre-prenom>/
git commit -m "feat: module <votre-prenom> termine"
git push origin <votre-branche>
```

### 23h25 - Fusion sur `main`

Ouvrir les Pull Requests sur GitHub et fusionner les 3 branches dans `main` (aucun conflit attendu puisque les packages sont distincts).

### 23h35 - Recette complète

Vider les tables (`TRUNCATE TABLE matchs, stades, pays;` avec réinitialisation des auto-increment en MySQL), lancer `MainFrame.java` et vérifier le flux complet : Équipes > Stades > Matchs > Scores > Classements.

### 23h50 - Rendu final

Exporter l'archive finale (`.zip` contenant le code source, `lib/` et `database.sql`) et livrer avant 00h00.

## Configuration rapide de la bd
-- 1. Créer la base si ce n'est pas encore fait
```CREATE DATABASE IF NOT EXISTS coupe_du_monde CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;```

-- 2. Créer un utilisateur commun pour l'équipe
```CREATE USER IF NOT EXISTS 'user_cdm'@'localhost' IDENTIFIED BY 'cdm2022';```

-- 3. Lui donner tous les droits uniquement sur la base du projet
```GRANT ALL PRIVILEGES ON coupe_du_monde.* TO 'user_cdm'@'localhost';```

-- 4. Appliquer immédiatement les privilèges
```FLUSH PRIVILEGES;```

## Créer une application Linux exécutable

Sur Linux x86_64 avec un JDK 17+ (incluant `jpackage`), lancer :

```bash
chmod +x package-linux.sh
./package-linux.sh
```

Le lanceur autonome sera créé ici :

```bash
./dist/CoupeDuMonde/bin/CoupeDuMonde
```

Le dossier complet peut être transféré via l'archive :

```text
dist/CoupeDuMonde-linux-x86_64.zip
```

L'image inclut un runtime Java et le pilote JDBC. Elle ne contient pas le serveur
MySQL : celui-ci doit être installé, la base `coupe_du_monde` initialisée avec
`database.sql`, et les identifiants configurés avant d'utiliser l'application.
Cette image est construite pour Linux x86_64 ; Windows et macOS nécessitent un
empaquetage distinct sur leur système cible.

## Créer une application Windows exécutable

Sur Windows x64, installer un JDK 17+ contenant `jpackage`, ouvrir PowerShell
dans le dossier du projet et exécuter :

```powershell
.\package-windows.ps1
```

Si l'exécution des scripts est bloquée dans la session, utiliser :

```powershell
powershell -ExecutionPolicy Bypass -File .\package-windows.ps1
```

Le lanceur autonome sera créé à `dist\CoupeDuMonde\CoupeDuMonde.exe` et le
dossier complet pourra être partagé dans `dist\CoupeDuMonde-windows-x64.zip`.
Le paquet embarque le runtime Java et le pilote JDBC, mais pas le serveur MySQL.
Il faut construire ce paquet sur Windows ; le paquet Linux n'est pas compatible.
C'est une application portable et non un installateur MSI : extraire le ZIP puis
lancer `CoupeDuMonde.exe`.

## Exécution 
J'ai ajouté des scripts pour la compilation et l'exécution
Rendez les exécutable
    ```chmod +x build.sh run.sh```

A chaque modification faites simplement
    ```./build.sh && ./run.sh``` 