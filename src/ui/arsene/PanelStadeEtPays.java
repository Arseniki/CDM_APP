package ui.arsene;

import db.ConnexionDB;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
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
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.DefaultListCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
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
                new Object[]{"", "Équipe", "Groupe"}, 0) {
            @Override
            public boolean isCellEditable(int ligne, int colonne) {
                return false;
            }
        };
        JTable tableEquipes = new JTable(modeleEquipes);
        tableEquipes.setPreferredScrollableViewportSize(
                new Dimension(450, 96));
        tableEquipes.getColumnModel().getColumn(0).setMaxWidth(50);
        tableEquipes.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableEquipes.getColumnModel().getColumn(0).setCellRenderer(
                new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table, Object valeur, boolean isSelected,
                            boolean hasFocus, int ligne, int colonne) {
                        JLabel rendu = (JLabel) super.getTableCellRendererComponent(
                                table, "", isSelected, hasFocus, ligne, colonne);
                        rendu.setIcon(valeur instanceof Icon ? (Icon) valeur : null);
                        rendu.setHorizontalAlignment(JLabel.CENTER);
                        rendu.setText("");
                        return rendu;
                    }
                });
        sectionEquipes.add(UiTheme.creerScrollTable(tableEquipes), BorderLayout.CENTER);

        JPanel sectionAjout = UiTheme.creerSection("2. Ajouter un stade");
        JPanel formulaire = new JPanel(new GridBagLayout());
        formulaire.setBackground(UiTheme.COULEUR_FOND);
        GridBagConstraints contraintes = new GridBagConstraints();
        contraintes.insets = new Insets(UiTheme.MARGE_INTERNE, 0, 0, UiTheme.MARGE_INTERNE);
        contraintes.fill = GridBagConstraints.HORIZONTAL;
        contraintes.weightx = 1.0;

        comboPays = new JComboBox<>();
        activerRenduPays(comboPays);
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
        table.getColumnModel().getColumn(1).setCellRenderer(
                new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table, Object valeur, boolean isSelected,
                            boolean hasFocus, int ligne, int colonne) {
                        String pays = valeur == null ? "" : valeur.toString();
                        JLabel rendu = (JLabel) super.getTableCellRendererComponent(
                                table, pays, isSelected, hasFocus, ligne, colonne);
                        rendu.setIcon(creerDrapeau(pays));
                        return rendu;
                    }
                });
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        table.getColumnModel().getColumn(1).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);

        JPanel barreFiltres = new JPanel(new FlowLayout(FlowLayout.LEFT,
                UiTheme.MARGE_INTERNE, 0));
        barreFiltres.setBackground(UiTheme.COULEUR_FOND);
        barreFiltres.add(creerEtiquette("Pays"));
        filtrePays = new JComboBox<>();
        filtrePays.addItem("Tous les pays");
        activerRenduPays(filtrePays);
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

    private void activerRenduPays(JComboBox<String> liste) {
        liste.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> table, Object valeur, int ligne,
                    boolean isSelected, boolean hasFocus) {
                String texte = valeur == null ? "" : valeur.toString();
                Icon icone = null;
                if (!"Tous les pays".equals(texte)) {
                    icone = creerDrapeau(texte);
                }
                JLabel rendu = (JLabel) super.getListCellRendererComponent(
                        table, texte, ligne, isSelected, hasFocus);
                rendu.setIcon(icone);
                return rendu;
            }
        });
    }


    private void verifierFormulaire() {
        boutonEnregistrer.setEnabled(
                comboPays.getSelectedItem() != null
                        && !champVille.getText().trim().isEmpty()
                        && !champNomStade.getText().trim().isEmpty());
    }

    private Icon creerDrapeau(String pays) {
        return new DrapeauPays(pays);
    }

    private static final class DrapeauPays implements Icon {
        private final String pays;

        private DrapeauPays(String pays) {
            this.pays = pays;
        }

        @Override
        public int getIconWidth() {
            return 24;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }

        @Override
        public void paintIcon(Component composant, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            if ("Qatar".equals(pays)) {
                g.setColor(Color.WHITE);
                g.fillRect(x, y, 24, 16);
                g.setColor(new Color(0x8A1538));
                g.fillRect(x + 8, y, 16, 16);
                g.setColor(Color.WHITE);
                for (int i = 0; i < 8; i++) {
                    int dy = y + i * 2;
                    g.fillPolygon(new int[]{x + 6, x + 10, x + 6},
                            new int[]{dy, dy + 1, dy + 2}, 3);
                }
            } else if ("Équateur".equals(pays)) {
                g.setColor(new Color(0xFFD100));
                g.fillRect(x, y, 24, 8);
                g.setColor(new Color(0x0033A0));
                g.fillRect(x, y + 8, 24, 4);
                g.setColor(new Color(0xCE1126));
                g.fillRect(x, y + 12, 24, 4);
            } else if ("Sénégal".equals(pays)) {
                g.setColor(new Color(0x00853F));
                g.fillRect(x, y, 8, 16);
                g.setColor(new Color(0xFDEF42));
                g.fillRect(x + 8, y, 8, 16);
                g.setColor(new Color(0xE31B23));
                g.fillRect(x + 16, y, 8, 16);
                g.setColor(new Color(0x00853F));
                g.fillOval(x + 10, y + 6, 4, 4);
            } else if ("Pays-Bas".equals(pays)) {
                bandes(g, x, y, new Color(0xAE1C28),
                        Color.WHITE, new Color(0x21468B));
            } else if ("Angleterre".equals(pays)) {
                g.setColor(Color.WHITE);
                g.fillRect(x, y, 24, 16);
                g.setColor(new Color(0xCE1124));
                g.fillRect(x + 10, y, 4, 16);
                g.fillRect(x, y + 6, 24, 4);
            } else if ("Iran".equals(pays)) {
                bandes(g, x, y, new Color(0x239F40),
                        Color.WHITE, new Color(0xDA0000));
            } else if ("États-Unis".equals(pays)) {
                bandes(g, x, y, new Color(0xB22234),
                        Color.WHITE, new Color(0xB22234));
                g.setColor(new Color(0x3C3B6E));
                g.fillRect(x, y, 10, 9);
            } else if ("Pays de Galles".equals(pays)) {
                g.setColor(Color.WHITE);
                g.fillRect(x, y, 24, 8);
                g.setColor(new Color(0x00A3A1));
                g.fillRect(x, y + 8, 24, 8);
                g.setColor(new Color(0xCE1124));
                g.fillOval(x + 8, y + 4, 8, 8);
            } else {
                g.setColor(new Color(0xC0C0C0));
                g.fillRect(x, y, 24, 16);
            }

            g.setColor(new Color(0x808080));
            g.drawRect(x, y, 23, 15);
            g.dispose();
        }

        private void bandes(Graphics2D g, int x, int y,
                Color haut, Color milieu, Color bas) {
            g.setColor(haut);
            g.fillRect(x, y, 24, 6);
            g.setColor(milieu);
            g.fillRect(x, y + 6, 24, 4);
            g.setColor(bas);
            g.fillRect(x, y + 10, 24, 6);
        }
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
                    String pays = resultat.getString("nom_pays");
                    modeleEquipes.addRow(new Object[]{
                            creerDrapeau(pays),
                            pays,
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
