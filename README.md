🏆 Projet Java Swing : Gestion de la Coupe du Monde de Football

Application bureau développée en Java Swing connectée à une base de données PostgreSQL permettant de gérer les équipes, les stades, la planification des matchs, la saisie des scores et le calcul dynamique des classements.
• Équipe : Arsène (Lead / Intégration), Mohamine, Martin
• Date de livraison : Aujourd'hui à 00h00
• Stack technique : Java (JDK 8+), Java Swing, PostgreSQL, JDBC (postgresql.jar), Git / GitHub.

✅ 1. État d'avancement actuel (Ce qui est déjà fait)

L'architecture de base est en place sur la branche main :
• Création du dépôt GitHub et configuration du .gitignore.
• Script SQL complet de création des tables (PAYS, STADE, MATCH) et données de test (database.sql).
• Pilote JDBC PostgreSQL ajouté dans le dossier lib/.
• Classe de connexion centralisée (src/db/ConnexionDB.java).
• Fenêtre principale à onglets (src/ui/MainFrame.java) déjà reliée aux 3 modules.
• Squelettes des 3 panneaux individuels (PanelStadeEtPays, PanelMatchs, PanelClassement).

⚙️ 2. Guide de démarrage rapide (Pour Mohamine & Martin)

Suivez ces 4 étapes dès que vous clonez le projet :

Étape 1 : Récupérer le projet et créer sa branche

Ouvrez un terminal et exécutez :
bash
git clone <URL_DU_DEPOT_GITHUB>
cd <NOM_DU_DOSSIER>

# Créez votre branche personnelle (NE PAS CODER SUR MAIN) :
git checkout -b feat/mohamine-matchs      # Pour Mohamine
# OU
git checkout -b feat/martin-classement    # Pour Martin
Utilisez le code avec précaution.

Étape 2 : Configurer la base de données PostgreSQL

1. Ouvrez pgAdmin et créez une base de données nommée exactement : coupe_du_monde.
2. Ouvrez l'outil de requête (Query Tool) sur cette base et exécutez tout le contenu du fichier database.sql situé à la racine du projet.
3. Vérifiez vos identifiants dans src/db/ConnexionDB.java (par défaut : utilisateur postgres, mot de passe admin). Attention : ne poussez pas votre mot de passe personnel si vous le modifiez.

Étape 3 : Lier le pilote PostgreSQL (lib/*.jar) dans votre IDE

• Sur VS Code : Dans la barre latérale gauche, ouvrez JAVA PROJECTS → Referenced Libraries → cliquez sur + → sélectionnez le fichier .jar dans le dossier lib/.
• Sur IntelliJ : Clic droit sur le fichier .jar dans lib/ → Add as Library....
• Sur Eclipse : Clic droit sur le fichier .jar → Build Path → Add to Build Path.

Étape 4 : Tester le lancement

Exécutez le fichier src/ui/MainFrame.java. Une fenêtre avec 3 onglets doit s'ouvrir sans erreur.

👨‍💻 3. Répartition des tâches & Cahier des charges (21h45 - 23h15)

Règle d'or anti-conflit : Chacun travaille exclusivement dans son propre dossier src/ui/<prenom>/.

🔹 ARSÈNE (feat/arsene-stades) — Équipes & Stades (Questions 1 & 2)

• Fichier : src/ui/arsene/PanelStadeEtPays.java
• À développer :
	• Instanciation des 8 équipes (Q1) : Bouton ou méthode au démarrage insérant les 8 équipes de 2022 (Qatar, Équateur, Sénégal, Pays-Bas en Groupe A ; Angleterre, Iran, États-Unis, Pays de Galles en Groupe B) avec ON CONFLICT DO NOTHING.
	• Ajout de stade (Q2) : Formulaire (Pays, Ville, NomStade) + bouton Enregistrer insérant dans la table STADE.
	• Affichage : Une JTable listant les stades enregistrés, mise à jour automatiquement après chaque ajout.

🔹 MOHAMINE (feat/mohamine-matchs) — Planification & Scores (Question 3)

• Fichier : src/ui/mohamine/PanelMatchs.java
• À développer :
	• Planifier un match : Deux listes déroulantes (JComboBox) pour NomPays1 et NomPays2 chargées depuis PAYS, une JComboBox pour le stade (STADE), et un champ date (YYYY-MM-DD via java.sql.Date.valueOf()).
	• Validation métier : Empêcher un pays de jouer contre lui-même (NomPays1.equals(NomPays2)). Les buts restent à NULL lors de la planification.
	• Saisir les scores : Une JTable affichant tous les matchs. Permettre de sélectionner un match et d'enregistrer/modifier Pays1Buts et Pays2Buts (UPDATE MATCH SET ... WHERE IDMatch = ?).

🔹 MARTIN (feat/martin-classement) — Moteur de Classement (Question 4)

• Fichier : src/ui/martin/PanelClassement.java
• À développer :
	• Note : Insérez 2 ou 3 faux matchs avec des scores dans pgAdmin pour tester vos calculs sans attendre Mohamine.
	• Calcul des statistiques : Récupérer tous les pays (PAYS) et les matchs joués (WHERE Pays1Buts IS NOT NULL AND Pays2Buts IS NOT NULL). Calculer pour chaque équipe : MJ, G, N, P, BP, BC, Diff (BP - BC), Points (G*3 + N*1).
	• Tri : Ordonner par Points décroissants, puis Différence de buts décroissante.
	• Interface : Sélecteur (Groupe A, Groupe B, Classement Général) + JTable + bouton Actualiser pour recharger les données.

🔄 4. Intégration Finale & Livraison (23h15 - 00h00)

1. 23h15 — Envoi du code sur GitHub :bash
git add src/ui/<votre-prenom>/
git commit -m "feat: module <votre-prenom> termine"
git push origin <votre-branche>
Utilisez le code avec précaution.
2. 23h25 — Fusion sur main :
	• Ouvrir les Pull Requests sur GitHub et fusionner les 3 branches dans main (0 conflit garanti car les packages sont distincts).
3. 23h35 — Recette complète :
	• Vider les tables (TRUNCATE MATCH, STADE, PAYS RESTART IDENTITY;), lancer MainFrame.java et vérifier le flux complet : Équipes → Stades → Matchs → Scores → Classements.
4. 23h50 — Rendu final :
	• Exportation de l'archive finale (.zip contenant le code source, lib/ et database.sql) et livraison avant 00h00.

