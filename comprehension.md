# Documentation technique - Coupe du Monde 2022

## 1. Objectif et périmètre

Cette application de bureau Java Swing permet de gérer les équipes, les stades,
les matchs, les scores et les classements de la Coupe du Monde 2022. Les données
sont conservées dans MySQL et manipulées en JDBC.

Le parcours métier principal est :

1. initialiser les pays et enregistrer les stades ;
2. planifier les matchs dans ces stades ;
3. saisir les scores une fois les matchs joués ;
4. consulter les classements calculés depuis les résultats.

## 2. Architecture du projet

| Fichier / package | Responsabilité |
|---|---|
| `src/ui/MainFrame.java` | Fenêtre principale, navigation entre modules et démarrage graphique. |
| `src/ui/arsene/PanelStadeEtPays.java` | Initialisation des équipes, ajout et recherche de stades. |
| `src/ui/mohamine/PanelMatchs.java` | Planification de matchs, consultation, recherche et saisie des scores. |
| `src/ui/martin/PanelClassement.java` | Calcul et affichage des statistiques et classements. |
| `src/ui/design/UiTheme.java` | Couleurs, polices, espacements et composants Swing communs. |
| `src/db/ConnexionDB.java` | Création et fermeture de la connexion JDBC MySQL. |
| `database.sql` | Création des tables, contraintes et données de démonstration. |
| `build.sh`, `run.sh` | Compilation des sources puis lancement avec le pilote JDBC. |
| `package-linux.sh` | Construction d’une image d’application Linux avec `jpackage`. |
| `package-windows.ps1` | Construction d’une image portable Windows, à exécuter sous Windows. |

Les panneaux sont intégrés dans `MainFrame` comme cartes d’un `CardLayout`.
Le changement de module ne détruit donc pas la fenêtre principale ; les boutons
de navigation affichent simplement un autre panneau.

## 3. Modèle de données

Le script `database.sql` définit trois tables :

### `pays`

- `nom_pays` : clé primaire, nom de l’équipe ;
- `groupe` : groupe `A` ou `B`.

### `stades`

- `id_stade` : identifiant auto-incrémenté ;
- `pays`, `ville`, `nom_stade` : localisation et nom ;
- contrainte d’unicité sur `(pays, ville, nom_stade)`.

### `matchs`

- `id_match` : identifiant auto-incrémenté ;
- `nom_pays_1`, `nom_pays_2` : clés étrangères vers `pays` ;
- `id_stade` : clé étrangère vers `stades` ;
- `date_match` : date de la rencontre ;
- `pays_1_buts`, `pays_2_buts` : scores, initialement `NULL`.

Les contraintes interdisent notamment les matchs d’un pays contre lui-même,
les buts négatifs et un score où un seul des deux nombres de buts est renseigné.

## 4. Démarrage et navigation - `MainFrame`

### Constructeur `MainFrame()`

Configure le titre, la taille et la fermeture de la fenêtre. Il construit le
bandeau supérieur, les commandes de navigation, les trois panneaux, la barre
d’état, puis installe les raccourcis clavier. Le premier module affiché est
« Équipes & Stades ».

### `creerNavigation()`

Crée trois boutons de navigation sélectionnables, disposés horizontalement.
Chaque bouton contient un numéro, le nom du module, une courte description et
une infobulle indiquant son raccourci.

### `installerNavigation()`

Associe l’action de chaque bouton à `afficherModule(index)`.

### `afficherModule(int index)`

Affiche la carte correspondante dans le `CardLayout`, marque son bouton comme
actif et met à jour les couleurs, la bordure et le texte de la barre d’état.

### `creerBandeau()`

Construit l’en-tête du programme : ballon dessiné, titre, sous-titre et numéro
d’édition. Les couleurs viennent de `UiTheme`.

### `creerPied()`

Construit le pied de fenêtre. Il contient le statut du module actif et le rappel
des raccourcis clavier.

### `installerRaccourcis(JPanel racine)`

Associe `Ctrl+1`, `Ctrl+2` et `Ctrl+3` aux trois modules. Les touches équivalentes
du pavé numérique sont aussi enregistrées. Les actions sont installées sur la
fenêtre et le panneau racine afin de rester accessibles lorsque le focus est
dans un composant du formulaire.

### Icône interne `NumeroModuleIcon`

- `getIconWidth()` et `getIconHeight()` : définissent la taille de l’icône
  circulaire numérotée.
- `paintIcon(...)` : dessine le numéro dans un cercle ; la couleur dépend de
  l’état sélectionné du bouton.

### Icône interne `BallonIcon`

- `getIconWidth()` et `getIconHeight()` : définissent la place du ballon.
- `paintIcon(...)` : dessine le ballon avec des formes Java2D et les couleurs
  du thème.

### `main(String[] args)`

Choisit le look-and-feel Swing multiplateforme, puis crée et affiche la fenêtre
via `SwingUtilities.invokeLater`, sur le thread d’interface Swing.

## 5. Équipes et stades - `PanelStadeEtPays`

### Constructeur `PanelStadeEtPays()`

Assemble les sections « Équipes », « Ajouter un stade » et « Stades
enregistrés ». Il crée les tables, les filtres et les contrôles du formulaire,
puis charge les pays, les équipes et les stades depuis la base.

### `creerEtiquette(String texte)`

Crée un libellé de formulaire utilisant la police et la couleur communes.

### `activerRenduPays(JComboBox<String> liste)`

Installe un renderer Swing qui affiche un drapeau graphique à côté du nom du pays
dans la liste déroulante. Les drapeaux sont dessinés en Java plutôt que rendus
comme des emoji, afin d’éviter leur remplacement par deux lettres selon la
police du système.

### `verifierFormulaire()`

Active le bouton d’enregistrement du stade seulement si un pays est sélectionné
et que les champs ville et nom du stade ne sont pas vides.

### `creerDrapeau(String pays)`

Fabrique l’icône correspondant au pays demandé.

### Classe interne `DrapeauPays`

- `getIconWidth()` et `getIconHeight()` : indiquent la taille de l’icône.
- `paintIcon(...)` : dessine le drapeau du pays avec Java2D.
- `bandes(...)` : dessine les trois bandes horizontales utilisées par certains
  drapeaux.

### `initialiserEquipes()`

Insère les huit équipes des groupes A et B à l’aide d’un `PreparedStatement`
batch et de `INSERT IGNORE`. Cette insertion peut être relancée sans dupliquer
les pays existants. Après réussite, les listes et le tableau des équipes sont
rechargés.

### `enregistrerStade()`

Lit les valeurs du formulaire, valide le pays, la ville et le nom, puis insère
le stade avec un `PreparedStatement`. En cas de succès, vide les champs et
actualise la table. Une violation d’unicité est signalée à l’utilisateur.

### `chargerPays()`

Recharge les pays disponibles dans la liste du formulaire et dans le filtre des
stades. Elle essaie de conserver le filtre de pays qui était sélectionné.

### `actualiserEquipes()`

Exécute une requête triant les pays par groupe et par nom, puis remplit le
tableau des équipes avec le drapeau, le nom du pays et le groupe.

### `appliquerFiltres()`

Combine la recherche textuelle et le filtre de pays avec un `RowFilter`.
La recherche est insensible à la casse ; le filtre de pays doit correspondre
exactement. Met ensuite à jour le nombre de stades visibles.

### `actualiserStades()`

Vide puis recharge la table des stades (ID, pays, ville, nom), avant de
réappliquer les filtres courants.

### `mettreAJourStatut()`

Affiche le nombre de stades visibles après filtrage, avec le singulier ou le
pluriel approprié.

### `afficherInformation(String message)`

Affiche une boîte de dialogue informative.

### `afficherErreur(String contexte, SQLException e)`

Affiche le contexte de l’échec et le message SQL dans une boîte d’erreur.

## 6. Matchs et scores - `PanelMatchs`

### Classe interne `StadeItem`

Représente un stade choisi dans une liste ou présent dans le tableau, en
conservant son identifiant, son nom, sa ville et son pays.

- `StadeItem(int idStade, String nomStade, String ville)` : constructeur
  compatible sans donnée de pays.
- `StadeItem(int idStade, String nomStade, String ville, String pays)` :
  constructeur complet utilisé pour montrer le drapeau du pays.
- `getIdStade()`, `getNomStade()`, `getVille()`, `getPays()` : accesseurs.
- `toString()` : fournit le libellé visible `nom du stade (ville)`.

### Constructeur `PanelMatchs()`

Crée l’en-tête du module et organise les sections de planification, de liste des
matchs, de saisie des scores et d’actualisation. Installe les écouteurs puis
charge les données initiales.

### `creerSectionPlanification()`

Construit le formulaire de planification : deux listes de pays, une liste de
stades, un champ date au format `AAAA-MM-JJ` et le bouton de planification.
Les listes de pays et de stades utilisent des renderers avec drapeaux.

### `creerSectionTableau()`

Construit le modèle non éditable et le tableau des matchs. Ajoute les colonnes
ID, équipes, scores, stade et date, configure les largeurs, la sélection, le
tri et la recherche, ainsi que les compteurs de rencontres jouées/à jouer.

### `creerIndicateur(String titre, String valeur)`

Crée un libellé de synthèse en style HTML, utilisé pour présenter les compteurs.

### `creerSectionScores()`

Construit les commandes de saisie et modification du score : libellé du match,
deux `JSpinner` bornés entre 0 et 99 et bouton d’enregistrement, désactivé
jusqu’à la sélection d’un match.

### `creerBarreActions()`

Construit la barre contenant le bouton d’actualisation des données.

### `creerLabel(String texte)`

Crée un libellé de formulaire conforme au thème.

### `installerRenduPays(JComboBox<String> combo)`

Ajoute un drapeau dessiné à chaque entrée de la liste de pays.

### `installerRenduStades()`

Affiche le drapeau du pays et le libellé du stade dans la liste déroulante des
stades.

### `afficherErreur(String contexte, SQLException erreur)`

Présente explicitement à l’utilisateur le contexte de l’erreur JDBC et son
message.

### Renderer interne `RenduMatch`

`getTableCellRendererComponent(...)` contrôle l’alignement, les couleurs des
lignes alternées, l’affichage des drapeaux des pays/stades et la mise en valeur
des scores déjà saisis.

### Icône interne `DrapeauPays`

- `getIconWidth()` et `getIconHeight()` : définissent les dimensions du drapeau.
- `paintIcon(...)` : dessine le drapeau du pays avec Java2D.
- `bandes(...)` : dessine des bandes de couleurs horizontales.

### `installerEcouteurs()`

Relie les boutons à la planification, à l’enregistrement d’un score et à
l’actualisation. Les changements du champ de recherche actualisent le filtre ;
les changements de sélection du tableau appellent `onMatchSelectionChange()`.

### `chargerToutesLesDonnees()`

Point d’entrée de rechargement du formulaire et du tableau ; appelle
successivement `chargerPays()`, `chargerStades()` et `chargerMatchs()`.

### `chargerPays()`

Charge les noms des pays depuis MySQL et les insère dans les deux listes de
sélection. Elle préserve autant que possible la sélection courante.

### `chargerStades()`

Charge les stades avec leur identifiant, nom, ville et pays, puis construit les
objets `StadeItem` correspondants.

### `chargerMatchs()`

Charge les matchs avec leurs équipes, scores, stade et date. Les scores non
saisis sont affichés par `-`. La méthode compte les matchs joués, réinitialise
la sélection de score, met à jour les indicateurs et réapplique la recherche.

### `actualiserIndicateurs(int matchsJoues)`

Met à jour les nombres de rencontres totales, jouées et restant à jouer.

### `appliquerRecherche()`

Applique en temps réel un filtre insensible à la casse sur les valeurs des
colonnes et indique le nombre de résultats visibles par rapport au total.

### `onMatchSelectionChange()`

Convertit la ligne sélectionnée à l’écran en indice de ligne du modèle
(important lorsque le tri ou la recherche masque des lignes), retient l’ID du
match, affiche les équipes et initialise les deux champs de score.

### `planifierMatch()`

Valide les équipes, empêche une équipe d’affronter elle-même, exige un stade et
une date valide, puis insère le match avec les deux scores à `NULL`. La requête
est paramétrée ; la liste est rechargée après réussite.

### `enregistrerScore()`

Vérifie qu’un match est sélectionné, récupère les valeurs des deux `JSpinner`,
puis met à jour les scores de l’enregistrement correspondant à l’ID sélectionné.
Le tableau est rechargé après réussite.

## 7. Classements - `PanelClassement`

### Constructeur `PanelClassement()`

Construit le filtre Groupe A/Groupe B/Classement général, le bouton
d’actualisation, les cartes de synthèse (matchs joués, buts inscrits, équipe en
tête) et le tableau des statistiques. Charge le premier classement à
l’initialisation.

### `creerCarte(String titre, JLabel valeur)`

Crée une carte encadrée de synthèse avec un titre et une valeur.

### `creerValeurCarte()`

Crée un libellé prêt à afficher une valeur de carte avec la typographie du thème.

### `chargerClassement()`

Charge les pays du groupe sélectionné (ou de tous les groupes) et les matchs
dont les deux scores sont renseignés. Calcule les statistiques, trie les équipes
par points décroissants puis différence de buts décroissante et nom, remplit le
tableau, et met à jour les cartes. Une erreur SQL est affichée dans une boîte de
dialogue.

Les colonnes affichées sont :

- `MJ` : matchs joués ;
- `G`, `N`, `P` : gagnés, nuls, perdus ;
- `BP`, `BC` : buts pour et contre ;
- `Diff` : `BP - BC` ;
- `Pts` : `3 × victoires + nuls`.

### `appliquerMatch(Statistiques equipe1, Statistiques equipe2, int buts1, int buts2)`

Met à jour les statistiques des deux équipes en fonction du résultat : matchs
joués, buts marqués/encaissés, victoires, nuls ou défaites.

### Classe interne `Statistiques`

Stocke les statistiques calculées pour une équipe.

- `Statistiques(String nomEquipe, String groupe)` : initialise le nom et le
  groupe.
- `difference()` : calcule la différence de buts.
- `points()` : calcule les points selon le barème trois points pour une victoire
  et un point pour un nul.
- `nomEquipe()` : fournit le nom à utiliser comme dernier critère de tri.

### Renderer interne `RenduClassement`

`getTableCellRendererComponent(...)` affiche un drapeau à côté de chaque équipe,
alterne la couleur des lignes, met en évidence la première place et accentue la
colonne des points.

### Icône interne `DrapeauPays`

- `getIconWidth()` et `getIconHeight()` : définissent la taille du drapeau.
- `paintIcon(...)` : dessine les motifs des drapeaux avec Java2D.
- `bandes(...)` : dessine les bandes horizontales et permet de régler leurs
  proportions.

## 8. Thème commun - `UiTheme`

`UiTheme` est une classe utilitaire non instanciable. Les panneaux réutilisent
ses constantes et helpers afin de garder une présentation uniforme.

- `creerEntete(String titre)` : produit le bandeau foncé du haut d’un panneau.
- `creerBouton(String texte)` : produit un bouton d’action bleu.
- `creerSection(String titre)` : produit un panneau de section avec titre.
- `creerScrollTable(JTable table)` : applique police, hauteur, grille et fond à
  une table et l’enveloppe dans un `JScrollPane`.
- `creerPanneauRacine()` : crée le panneau racine avec fond et marges communs.

Les couleurs, polices et dimensions sont exposées comme constantes `public
static final`.

## 9. Accès aux données - `ConnexionDB`

### `getConnection()`

Charge le pilote JDBC MySQL si nécessaire et crée une connexion via
`DriverManager` à partir des paramètres configurés dans la classe. Si la
connexion singleton est absente ou fermée, elle est recréée. Une erreur de
pilote ou de connexion est remontée sous forme de `SQLException`.

### `close()`

Ferme la connexion si elle est ouverte et remet la référence interne à `null`.
Une éventuelle erreur de fermeture est écrite sur la sortie d’erreur.

## 10. Compilation, exécution et empaquetage

### `build.sh`

Supprime puis recrée `out/`, compile tous les fichiers `.java` dans `src/` avec
le contenu de `lib/` dans le classpath et s’arrête si la compilation échoue.

### `run.sh`

Lance `ui.MainFrame` avec les classes compilées et les bibliothèques de `lib/`.

### `package-linux.sh`

Compile le projet, crée le JAR, y associe le pilote JDBC, puis utilise `jpackage`
pour construire une image Linux incluant un runtime Java. L’image et son ZIP
sont écrits dans `dist/`.

### `package-windows.ps1`

Doit être exécuté sur Windows x64 avec un JDK 17+ et `jpackage` dans le `PATH`.
Il compile les sources, crée le JAR et produit une image portable contenant
`CoupeDuMonde.exe`, son runtime Java et le pilote JDBC.

Les images `jpackage` n’embarquent pas le serveur MySQL. Celui-ci doit être
accessible et la base doit être initialisée avec `database.sql`.

## 11. Flux d’exécution d’un exemple

1. `MainFrame.main()` démarre Swing sur le thread événementiel.
2. `MainFrame()` construit les panneaux ; leurs constructeurs chargent leurs
   données avec `ConnexionDB.getConnection()`.
3. L’utilisateur planifie une rencontre dans `PanelMatchs`. `planifierMatch()`
   valide la saisie, écrit le match dans MySQL puis recharge la liste.
4. Après sélection du match, `onMatchSelectionChange()` associe la sélection à
   son ID et prépare la saisie.
5. `enregistrerScore()` met à jour les buts dans `matchs`.
6. `PanelClassement.chargerClassement()` relit les matchs joués et recalcule les
   statistiques affichées.

