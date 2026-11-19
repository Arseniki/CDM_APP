package ui.arsene;

import db.ConnexionDB;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Dimension;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import ui.design.UiTheme;

public class PanelStadeEtPays extends JPanel {
    private final JComboBox<String> comboPays;
    private final JComboBox<String> filtrePays;
    private final JTextField champRecherche;
    private final JTextField champVille;
    private final JTextField champNomStade;
    private final DefaultTableModel modeleTable;
    private final DefaultTableModel modeleEquipes;
    private final JLabel statut;
    private final JButton boutonEnregistrer;
    private TableRowSorter<DefaultTableModel> trieurStades;

    public PanelStadeEtPays() {
        super(new BorderLayout());
        setBackground(UiTheme.COULEUR_FOND);

        JPanel racine = UiTheme.creerPanneauRacine();
        racine.add(UiTheme.creerEntete("Équipes & Stades"), BorderLayout.NORTH);

        JPanel contenu = new JPanel(new BorderLayout(0, UiTheme.ESPACE_VERTICAL));
        contenu.setBackground(UiTheme.COULEUR_FOND);

        JPanel sectionEquipes = UiTheme.creerSection("1. Équipes");
        JPanel actionsEquipes = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        actionsEquipes.setBackground(UiTheme.COULEUR_FOND);
        JButton boutonEquipes = UiTheme.creerBouton("Créer les 8 équipes");
        boutonEquipes.addActionListener(event -> initialiserEquipes());
        actionsEquipes.add(boutonEquipes);
        sectionEquipes.add(actionsEquipes, BorderLayout.NORTH);

        modeleEquipes = new DefaultTableModel(
                new Object[]{"Équipe", "Groupe"}, 0) {
            @Override
            public boolean isCellEditable(int ligne, int colonne) {
                return false;
            }
        };
        JTable tableEquipes = new JTable(modeleEquipes);
        tableEquipes.setPreferredScrollableViewportSize(
                new Dimension(450, 96));
        sectionEquipes.add(UiTheme.creerScrollTable(tableEquipes), BorderLayout.CENTER);

        JPanel sectionAjout = UiTheme.creerSection("2. Ajouter un stade");
        JPanel formulaire = new JPanel(new GridBagLayout());
        formulaire.setBackground(UiTheme.COULEUR_FOND);
        GridBagConstraints contraintes = new GridBagConstraints();
        contraintes.insets = new Insets(UiTheme.MARGE_INTERNE, 0, 0, UiTheme.MARGE_INTERNE);
        contraintes.fill = GridBagConstraints.HORIZONTAL;
        contraintes.weightx = 1.0;

        comboPays = new JComboBox<>();
        champVille = new JTextField();
        champNomStade = new JTextField();
        boutonEnregistrer = UiTheme.creerBouton("Enregistrer");
        boutonEnregistrer.addActionListener(event -> enregistrerStade());
        boutonEnregistrer.setEnabled(false);

        DocumentListener validateur = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                verifierFormulaire();
            }
            @Override
            public void removeUpdate(DocumentEvent event) {
                verifierFormulaire();
            }
            @Override
            public void changedUpdate(DocumentEvent event) {
                verifierFormulaire();
            }
        };
        champVille.getDocument().addDocumentListener(validateur);
        champNomStade.getDocument().addDocumentListener(validateur);
        comboPays.addActionListener(event -> verifierFormulaire());

        contraintes.gridx = 0;
        contraintes.gridy = 0;
        contraintes.weightx = 0.35;
        formulaire.add(creerEtiquette("Pays"), contraintes);
        contraintes.gridx = 1;
        contraintes.weightx = 0.25;
        formulaire.add(creerEtiquette("Ville"), contraintes);
        contraintes.gridx = 2;
        contraintes.weightx = 0.40;
        formulaire.add(creerEtiquette("Nom du stade"), contraintes);

        contraintes.gridy = 1;
        contraintes.gridx = 0;
        contraintes.weightx = 0.35;
        formulaire.add(comboPays, contraintes);
        contraintes.gridx = 1;
        contraintes.weightx = 0.25;
        formulaire.add(champVille, contraintes);
        contraintes.gridx = 2;
        contraintes.weightx = 0.40;
        formulaire.add(champNomStade, contraintes);

        contraintes.gridx = 2;
        contraintes.gridy = 2;
        contraintes.weightx = 0;
        contraintes.fill = GridBagConstraints.NONE;
        contraintes.anchor = GridBagConstraints.EAST;
        formulaire.add(boutonEnregistrer, contraintes);
        sectionAjout.add(formulaire, BorderLayout.CENTER);

        JPanel zonesSuperieures = new JPanel(new BorderLayout(0, UiTheme.ESPACE_VERTICAL));
        zonesSuperieures.setBackground(UiTheme.COULEUR_FOND);
        zonesSuperieures.add(sectionEquipes, BorderLayout.NORTH);
        zonesSuperieures.add(sectionAjout, BorderLayout.CENTER);
        contenu.add(zonesSuperieures, BorderLayout.NORTH);

        JPanel sectionStades = UiTheme.creerSection("3. Stades enregistrés");
        modeleTable = new DefaultTableModel(
                new Object[]{"ID", "Pays", "Ville", "Nom du stade"}, 0) {
            @Override
            public boolean isCellEditable(int ligne, int colonne) {
                return false;
            }
        };
        JTable table = new JTable(modeleTable);
        table.setAutoCreateRowSorter(true);
        trieurStades = new TableRowSorter<>(modeleTable);
        table.setRowSorter(trieurStades);
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);

        JPanel barreFiltres = new JPanel(new FlowLayout(FlowLayout.LEFT,
                UiTheme.MARGE_INTERNE, 0));
        barreFiltres.setBackground(UiTheme.COULEUR_FOND);
        barreFiltres.add(creerEtiquette("Pays"));
        filtrePays = new JComboBox<>();
        filtrePays.addItem("Tous les pays");
        filtrePays.setPreferredSize(new Dimension(160, 26));
        filtrePays.addActionListener(event -> appliquerFiltres());
        barreFiltres.add(filtrePays);
        barreFiltres.add(creerEtiquette("Rechercher"));
        champRecherche = new JTextField(16);
        champRecherche.setPreferredSize(new Dimension(180, 26));
        champRecherche.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                appliquerFiltres();
            }
            @Override
            public void removeUpdate(DocumentEvent event) {
                appliquerFiltres();
            }
            @Override
            public void changedUpdate(DocumentEvent event) {
                appliquerFiltres();
            }
        });
        barreFiltres.add(champRecherche);
        sectionStades.add(barreFiltres, BorderLayout.NORTH);

        JScrollPane scroll = UiTheme.creerScrollTable(table);
        sectionStades.add(scroll, BorderLayout.CENTER);
        statut = new JLabel("0 stade enregistré");
        statut.setFont(UiTheme.POLICE_NORMALE);
        statut.setForeground(UiTheme.COULEUR_SECTION);
        JPanel pied = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        pied.setBackground(UiTheme.COULEUR_FOND);
        pied.add(statut);
        sectionStades.add(pied, BorderLayout.SOUTH);
        contenu.add(sectionStades, BorderLayout.CENTER);

        racine.add(contenu, BorderLayout.CENTER);
        add(racine, BorderLayout.CENTER);

        chargerPays();
        actualiserEquipes();
        actualiserStades();
    }

    private JLabel creerEtiquette(String texte) {
        JLabel etiquette = new JLabel(texte);
        etiquette.setFont(UiTheme.POLICE_NORMALE);
        etiquette.setForeground(UiTheme.COULEUR_SECTION);
        return etiquette;
    }

    private void verifierFormulaire() {
        boutonEnregistrer.setEnabled(
                comboPays.getSelectedItem() != null
                        && !champVille.getText().trim().isEmpty()
                        && !champNomStade.getText().trim().isEmpty());
    }

    private void initialiserEquipes() {
        String sql = "INSERT IGNORE INTO pays (nom_pays, groupe) VALUES (?, ?)";
        Object[][] equipes = {
                {"Qatar", "A"}, {"Équateur", "A"}, {"Sénégal", "A"}, {"Pays-Bas", "A"},
                {"Angleterre", "B"}, {"Iran", "B"}, {"États-Unis", "B"}, {"Pays de Galles", "B"}
        };

        try {
            Connection connexion = ConnexionDB.getConnection();
            try (PreparedStatement requete = connexion.prepareStatement(sql)) {
                for (Object[] equipe : equipes) {
                    requete.setString(1, (String) equipe[0]);
                    requete.setString(2, (String) equipe[1]);
                    requete.addBatch();
                }
                requete.executeBatch();
            }
            chargerPays();
            actualiserEquipes();
            JOptionPane.showMessageDialog(this,
                    "Les 8 équipes ont été ajoutées ou existaient déjà.",
                    "Équipes enregistrées",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            afficherErreur("Impossible d'ajouter les équipes.", e);
        }
    }

    private void enregistrerStade() {
        String pays = (String) comboPays.getSelectedItem();
        String ville = champVille.getText().trim();
        String nomStade = champNomStade.getText().trim();

        if (pays == null || pays.isEmpty()) {
            afficherInformation("Sélectionnez un pays.");
            return;
        }
        if (ville.isEmpty() || nomStade.isEmpty()) {
            afficherInformation("Saisissez la ville et le nom du stade.");
            return;
        }

        String sql = "INSERT INTO stades (pays, ville, nom_stade) VALUES (?, ?, ?)";
        try {
            Connection connexion = ConnexionDB.getConnection();
            try (PreparedStatement requete = connexion.prepareStatement(sql)) {
                requete.setString(1, pays);
                requete.setString(2, ville);
                requete.setString(3, nomStade);
                requete.executeUpdate();
            }

            champVille.setText("");
            champNomStade.setText("");
            actualiserStades();
            JOptionPane.showMessageDialog(this,
                    "Le stade a été enregistré.",
                    "Stade ajouté",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate")) {
                afficherInformation("Ce stade existe déjà pour ce pays et cette ville.");
            } else {
                afficherErreur("Impossible d'ajouter le stade.", e);
            }
        }
    }

    private void chargerPays() {
        comboPays.removeAllItems();
        String filtreCourant = (String) filtrePays.getSelectedItem();
        filtrePays.removeAllItems();
        filtrePays.addItem("Tous les pays");
        String sql = "SELECT nom_pays FROM pays ORDER BY nom_pays";

        try {
            Connection connexion = ConnexionDB.getConnection();
            try (Statement requete = connexion.createStatement();
                 ResultSet resultat = requete.executeQuery(sql)) {
                while (resultat.next()) {
                    String pays = resultat.getString("nom_pays");
                    comboPays.addItem(pays);
                    filtrePays.addItem(pays);
                }
            }
            if (filtreCourant != null) {
                for (int i = 0; i < filtrePays.getItemCount(); i++) {
                    if (filtreCourant.equals(filtrePays.getItemAt(i))) {
                        filtrePays.setSelectedItem(filtreCourant);
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Impossible de charger les pays : " + e.getMessage());
        }
        verifierFormulaire();
    }

    private void actualiserEquipes() {
        modeleEquipes.setRowCount(0);
        String sql = "SELECT nom_pays, groupe FROM pays "
                + "ORDER BY groupe, nom_pays";

        try {
            Connection connexion = ConnexionDB.getConnection();
            try (Statement requete = connexion.createStatement();
                 ResultSet resultat = requete.executeQuery(sql)) {
                while (resultat.next()) {
                    modeleEquipes.addRow(new Object[]{
                            resultat.getString("nom_pays"),
                            resultat.getString("groupe")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Impossible de charger les équipes : " + e.getMessage());
        }
    }

    private void appliquerFiltres() {
        if (trieurStades == null) {
            return;
        }

        List<RowFilter<Object, Object>> filtres = new ArrayList<>();
        String recherche = champRecherche.getText().trim();
        if (!recherche.isEmpty()) {
            filtres.add(RowFilter.regexFilter(
                    "(?i)" + java.util.regex.Pattern.quote(recherche)));
        }

        String paysSelectionne = (String) filtrePays.getSelectedItem();
        if (paysSelectionne != null
                && !"Tous les pays".equals(paysSelectionne)) {
            filtres.add(RowFilter.regexFilter(
                    "^" + java.util.regex.Pattern.quote(paysSelectionne) + "$", 1));
        }

        if (filtres.isEmpty()) {
            trieurStades.setRowFilter(null);
        } else {
            trieurStades.setRowFilter(RowFilter.andFilter(filtres));
        }
        mettreAJourStatut();
    }

    private void actualiserStades() {
        modeleTable.setRowCount(0);
        String sql = "SELECT id_stade, pays, ville, nom_stade "
                + "FROM stades ORDER BY pays, ville, nom_stade";

        try {
            Connection connexion = ConnexionDB.getConnection();
            try (Statement requete = connexion.createStatement();
                 ResultSet resultat = requete.executeQuery(sql)) {
                while (resultat.next()) {
                    modeleTable.addRow(new Object[]{
                            resultat.getInt("id_stade"),
                            resultat.getString("pays"),
                            resultat.getString("ville"),
                            resultat.getString("nom_stade")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("Impossible de charger les stades : " + e.getMessage());
        }
        appliquerFiltres();
    }

    private void mettreAJourStatut() {
        int nombre = trieurStades.getViewRowCount();
        statut.setText(nombre + (nombre <= 1
                ? " stade affiché"
                : " stades affichés"));
    }

    private void afficherInformation(String message) {
        JOptionPane.showMessageDialog(this, message, "Information",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void afficherErreur(String contexte, SQLException e) {
        JOptionPane.showMessageDialog(this,
                contexte + "\n" + e.getMessage(),
                "Erreur",
                JOptionPane.ERROR_MESSAGE);
    }
}
