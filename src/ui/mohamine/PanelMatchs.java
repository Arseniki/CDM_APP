package ui.mohamine;

import db.ConnexionDB;
import ui.design.UiTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.sql.*;
import java.util.regex.Pattern;

/**
 * Module Mohamine : Planification des matchs et saisie des scores (Question 3).
 * Intègre la charte graphique commune UiTheme.
 */
public class PanelMatchs extends JPanel {

    // Éléments de planification
    private JComboBox<String> cbPays1;
    private JComboBox<String> cbPays2;
    private JComboBox<StadeItem> cbStade;
    private JTextField tfDate;
    private JButton btnPlanifier;

    // Tableau des matchs
    private JTable tableMatchs;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> trieurMatchs;
    private JTextField tfRecherche;
    private JLabel lblResultatsRecherche;
    private JLabel lblNombreMatchs;
    private JLabel lblMatchsJoues;
    private JLabel lblMatchsAvenir;

    // Éléments de score
    private JLabel lblMatchSelectionne;
    private JSpinner spButs1;
    private JSpinner spButs2;
    private JButton btnEnregistrerScore;
    private JButton btnActualiser;

    private Integer selectedMatchId = null;

    /**
     * Encapsule l'id_stade et le libellé pour la JComboBox de stades.
     */
    public static class StadeItem {
        private final int idStade;
        private final String nomStade;
        private final String ville;
        private final String pays;

        public StadeItem(int idStade, String nomStade, String ville) {
            this(idStade, nomStade, ville, "");
        }

        public StadeItem(int idStade, String nomStade, String ville, String pays) {
            this.idStade = idStade;
            this.nomStade = nomStade;
            this.ville = ville;
            this.pays = pays;
        }

        public int getIdStade() {
            return idStade;
        }

        public String getNomStade() {
            return nomStade;
        }

        public String getVille() {
            return ville;
        }

        public String getPays() {
            return pays;
        }

        @Override
        public String toString() {
            return nomStade + " (" + ville + ")";
        }
    }

    public PanelMatchs() {
        super(new BorderLayout());
        setBackground(UiTheme.COULEUR_FOND);

        JPanel racine = UiTheme.creerPanneauRacine();
        racine.add(UiTheme.creerEntete("Matchs & Scores"), BorderLayout.NORTH);

        JPanel contenu = new JPanel(new BorderLayout(0, UiTheme.ESPACE_VERTICAL));
        contenu.setBackground(UiTheme.COULEUR_FOND);

        // 1. Section Planification
        contenu.add(creerSectionPlanification(), BorderLayout.NORTH);

        // 2. Section Tableau + Saisie Score
        JPanel centreEtSud = new JPanel(new BorderLayout(0, UiTheme.ESPACE_VERTICAL));
        centreEtSud.setBackground(UiTheme.COULEUR_FOND);

        centreEtSud.add(creerSectionTableau(), BorderLayout.CENTER);
        centreEtSud.add(creerSectionScores(), BorderLayout.SOUTH);

        contenu.add(centreEtSud, BorderLayout.CENTER);
        racine.add(contenu, BorderLayout.CENTER);

        // 3. Barre d'actions globale (bas)
        racine.add(creerBarreActions(), BorderLayout.SOUTH);

        add(racine, BorderLayout.CENTER);

        // Écouteurs d'événements
        installerEcouteurs();

        // Chargement initial des données
        chargerToutesLesDonnees();
    }

    private JPanel creerSectionPlanification() {
        JPanel section = UiTheme.creerSection("1. Planifier un match");

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UiTheme.COULEUR_FOND);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(UiTheme.MARGE_INTERNE, UiTheme.MARGE_INTERNE,
                UiTheme.MARGE_INTERNE, UiTheme.MARGE_INTERNE);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cbPays1 = new JComboBox<>();
        cbPays2 = new JComboBox<>();
        cbStade = new JComboBox<>();
        installerRenduPays(cbPays1);
        installerRenduPays(cbPays2);
        installerRenduStades();
        tfDate = new JTextField("2022-11-20", 10);
        btnPlanifier = UiTheme.creerBouton("Planifier le match");
        cbPays1.setFont(UiTheme.POLICE_NORMALE);
        cbPays2.setFont(UiTheme.POLICE_NORMALE);
        cbStade.setFont(UiTheme.POLICE_NORMALE);
        tfDate.setFont(UiTheme.POLICE_NORMALE);
        cbPays1.setToolTipText("Choisissez la première équipe");
        cbPays2.setToolTipText("Choisissez l'équipe adverse");
        cbStade.setToolTipText("Choisissez le lieu de la rencontre");
        tfDate.setToolTipText("Format attendu : AAAA-MM-JJ");

        // Ligne 0 : Pays 1 & Pays 2
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        form.add(creerLabel("Pays 1 :"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.5;
        form.add(cbPays1, gbc);

        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0;
        form.add(creerLabel("Pays 2 :"), gbc);

        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.5;
        form.add(cbPays2, gbc);

        // Ligne 1 : Stade & Date
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(creerLabel("Stade :"), gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.5;
        form.add(cbStade, gbc);

        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0;
        form.add(creerLabel("Date (YYYY-MM-DD) :"), gbc);

        gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 0.5;
        form.add(tfDate, gbc);

        // Ligne 2 : Bouton Planifier
        gbc.gridx = 3; gbc.gridy = 2; gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.LINE_END;
        form.add(btnPlanifier, gbc);

        section.add(form, BorderLayout.CENTER);
        return section;
    }

    private JPanel creerSectionTableau() {
        JPanel section = UiTheme.creerSection("2. Liste des matchs");

        String[] entetes = {"ID", "Pays 1", "Score 1", "Score 2", "Pays 2", "Stade", "Date"};
        tableModel = new DefaultTableModel(entetes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableMatchs = new JTable(tableModel);
        tableMatchs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableMatchs.setRowHeight(UiTheme.HAUTEUR_LIGNE_TABLE + 4);
        tableMatchs.setDefaultRenderer(Object.class, new RenduMatch());
        trieurMatchs = new TableRowSorter<>(tableModel);
        tableMatchs.setRowSorter(trieurMatchs);

        tableMatchs.getColumnModel().getColumn(0).setPreferredWidth(45);
        tableMatchs.getColumnModel().getColumn(0).setMaxWidth(60);
        tableMatchs.getColumnModel().getColumn(1).setPreferredWidth(145);
        tableMatchs.getColumnModel().getColumn(4).setPreferredWidth(145);
        tableMatchs.getColumnModel().getColumn(5).setPreferredWidth(180);

        JPanel contenu = new JPanel(new BorderLayout(0, UiTheme.ESPACE_VERTICAL));
        contenu.setBackground(UiTheme.COULEUR_FOND);
        JPanel barreTableau = new JPanel(new BorderLayout());
        barreTableau.setBackground(UiTheme.COULEUR_FOND);
        JPanel indicateurs = new JPanel(new FlowLayout(FlowLayout.LEFT,
                UiTheme.MARGE_INTERNE, 0));
        indicateurs.setBackground(UiTheme.COULEUR_FOND);
        lblNombreMatchs = creerIndicateur("RENCONTRES", "0");
        lblMatchsJoues = creerIndicateur("SCORE SAISI", "0");
        lblMatchsAvenir = creerIndicateur("À JOUER", "0");
        indicateurs.add(lblNombreMatchs);
        indicateurs.add(lblMatchsJoues);
        indicateurs.add(lblMatchsAvenir);
        JPanel recherche = new JPanel(new FlowLayout(FlowLayout.RIGHT,
                UiTheme.MARGE_INTERNE, 0));
        recherche.setBackground(UiTheme.COULEUR_FOND);
        JLabel labelRecherche = creerLabel("Rechercher");
        tfRecherche = new JTextField(18);
        tfRecherche.setFont(UiTheme.POLICE_NORMALE);
        tfRecherche.setToolTipText("Filtrer par équipe, score, stade ou date");
        lblResultatsRecherche = new JLabel("0 match");
        lblResultatsRecherche.setFont(UiTheme.POLICE_TABLE);
        lblResultatsRecherche.setForeground(UiTheme.COULEUR_SECTION);
        recherche.add(labelRecherche);
        recherche.add(tfRecherche);
        recherche.add(lblResultatsRecherche);
        barreTableau.add(indicateurs, BorderLayout.WEST);
        barreTableau.add(recherche, BorderLayout.EAST);
        contenu.add(barreTableau, BorderLayout.NORTH);
        contenu.add(UiTheme.creerScrollTable(tableMatchs), BorderLayout.CENTER);
        tableMatchs.setRowHeight(UiTheme.HAUTEUR_LIGNE_TABLE + 4);
        section.add(contenu, BorderLayout.CENTER);
        return section;
    }

    private JLabel creerIndicateur(String titre, String valeur) {
        JLabel indicateur = new JLabel("<html><b>" + titre + "</b>&nbsp;&nbsp;"
                + valeur + "</html>");
        indicateur.setFont(UiTheme.POLICE_NORMALE);
        indicateur.setForeground(UiTheme.COULEUR_ENTETE);
        return indicateur;
    }

    private JPanel creerSectionScores() {
        JPanel section = UiTheme.creerSection("3. Saisie / Modification du score");

        JPanel formScore = new JPanel(new FlowLayout(FlowLayout.CENTER,
                UiTheme.MARGE_EXTERNE, UiTheme.MARGE_INTERNE));
        formScore.setBackground(UiTheme.COULEUR_FOND);

        lblMatchSelectionne = new JLabel("Sélectionnez un match dans le tableau.");
        lblMatchSelectionne.setFont(UiTheme.POLICE_SECTION);
        lblMatchSelectionne.setForeground(UiTheme.COULEUR_ENTETE);

        spButs1 = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
        spButs2 = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
        spButs1.setPreferredSize(new Dimension(50, 24));
        spButs2.setPreferredSize(new Dimension(50, 24));
        spButs1.setToolTipText("Buts marqués par le premier pays");
        spButs2.setToolTipText("Buts marqués par le second pays");

        btnEnregistrerScore = UiTheme.creerBouton("Enregistrer le score");
        btnEnregistrerScore.setEnabled(false);

        formScore.add(lblMatchSelectionne);
        formScore.add(creerLabel("Buts Pays 1 :"));
        formScore.add(spButs1);
        formScore.add(creerLabel("-"));
        formScore.add(creerLabel("Buts Pays 2 :"));
        formScore.add(spButs2);
        formScore.add(btnEnregistrerScore);

        section.add(formScore, BorderLayout.CENTER);
        return section;
    }

    private JPanel creerBarreActions() {
        JPanel barre = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        barre.setBackground(UiTheme.COULEUR_FOND);

        btnActualiser = UiTheme.creerBouton("Actualiser");
        barre.add(btnActualiser);

        return barre;
    }

    private JLabel creerLabel(String texte) {
        JLabel label = new JLabel(texte);
        label.setFont(UiTheme.POLICE_NORMALE);
        label.setForeground(UiTheme.COULEUR_SECTION);
        return label;
    }

    private void installerRenduPays(JComboBox<String> combo) {
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> liste, Object valeur, int index,
                    boolean selectionne, boolean focus) {
                JLabel rendu = (JLabel) super.getListCellRendererComponent(
                        liste, valeur, index, selectionne, focus);
                String pays = valeur == null ? "" : valeur.toString();
                rendu.setIcon(pays.isEmpty() ? null : new DrapeauPays(pays));
                return rendu;
            }
        });
    }

    private void installerRenduStades() {
        cbStade.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> liste, Object valeur, int index,
                    boolean selectionne, boolean focus) {
                JLabel rendu = (JLabel) super.getListCellRendererComponent(
                        liste, valeur, index, selectionne, focus);
                if (valeur instanceof StadeItem) {
                    StadeItem stade = (StadeItem) valeur;
                    rendu.setIcon(stade.getPays().isEmpty()
                            ? null : new DrapeauPays(stade.getPays()));
                    rendu.setText(stade.toString());
                } else {
                    rendu.setIcon(null);
                }
                return rendu;
            }
        });
    }

    private void afficherErreur(String contexte, SQLException erreur) {
        JOptionPane.showMessageDialog(this,
                contexte + "\n" + erreur.getMessage(),
                "Erreur de base de données",
                JOptionPane.ERROR_MESSAGE);
    }

    private static final class RenduMatch extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object valeur, boolean selectionne,
                boolean focus, int ligne, int colonne) {
            super.getTableCellRendererComponent(
                    table, valeur, selectionne, focus, ligne, colonne);
            setHorizontalAlignment(colonne == 1 || colonne == 4 || colonne == 5
                    ? SwingConstants.LEFT : SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(0,
                    UiTheme.MARGE_INTERNE, 0, UiTheme.MARGE_INTERNE));
            setFont(UiTheme.POLICE_TABLE);

            if (colonne == 1 || colonne == 4) {
                setIcon(valeur == null ? null : new DrapeauPays(valeur.toString()));
                setIconTextGap(UiTheme.MARGE_INTERNE);
            } else if (colonne == 5 && valeur instanceof StadeItem) {
                StadeItem stade = (StadeItem) valeur;
                setIcon(stade.getPays().isEmpty()
                        ? null : new DrapeauPays(stade.getPays()));
                setIconTextGap(UiTheme.MARGE_INTERNE);
                setText(stade.toString());
            } else {
                setIcon(null);
            }

            if (!selectionne) {
                setBackground(ligne % 2 == 0
                        ? Color.WHITE : UiTheme.COULEUR_TABLE_ALT);
                setForeground(UiTheme.COULEUR_SECTION);
                if ((colonne == 2 || colonne == 3)
                        && valeur != null && !"-".equals(valeur.toString())) {
                    setForeground(UiTheme.COULEUR_ENTETE);
                    setFont(UiTheme.POLICE_SECTION);
                }
                if ((colonne == 2 || colonne == 3)
                        && "-".equals(String.valueOf(valeur))) {
                    setForeground(UiTheme.COULEUR_BORDURE);
                }
            }
            return this;
        }
    }

    private static final class DrapeauPays implements Icon {
        private final String pays;

        private DrapeauPays(String pays) {
            this.pays = pays;
        }

        @Override
        public int getIconWidth() {
            return 26;
        }

        @Override
        public int getIconHeight() {
            return 18;
        }

        @Override
        public void paintIcon(Component composant, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            switch (pays) {
                case "Qatar":
                    g.setColor(Color.WHITE);
                    g.fillRect(x, y, 26, 18);
                    g.setColor(new Color(0x8A1538));
                    g.fillRect(x + 9, y, 17, 18);
                    for (int i = 0; i < 9; i++) {
                        int dy = y + i * 2;
                        g.setColor(Color.WHITE);
                        g.fillPolygon(new int[]{x + 7, x + 11, x + 7},
                                new int[]{dy, dy + 1, dy + 2}, 3);
                    }
                    break;
                case "Équateur":
                    bandes(g, x, y, new Color(0xFFD100),
                            new Color(0x0033A0), new Color(0xCE1126), true);
                    break;
                case "Sénégal":
                    g.setColor(new Color(0x00853F));
                    g.fillRect(x, y, 9, 18);
                    g.setColor(new Color(0xFDEF42));
                    g.fillRect(x + 9, y, 8, 18);
                    g.setColor(new Color(0xE31B23));
                    g.fillRect(x + 17, y, 9, 18);
                    g.setColor(new Color(0x00853F));
                    g.fillOval(x + 11, y + 7, 4, 4);
                    break;
                case "Pays-Bas":
                    bandes(g, x, y, new Color(0xAE1C28),
                            Color.WHITE, new Color(0x21468B), false);
                    break;
                case "Angleterre":
                    g.setColor(Color.WHITE);
                    g.fillRect(x, y, 26, 18);
                    g.setColor(new Color(0xCE1124));
                    g.fillRect(x + 11, y, 4, 18);
                    g.fillRect(x, y + 7, 26, 4);
                    break;
                case "Iran":
                    bandes(g, x, y, new Color(0x239F40),
                            Color.WHITE, new Color(0xDA0000), false);
                    break;
                case "États-Unis":
                    for (int i = 0; i < 7; i++) {
                        g.setColor(new Color(0xB22234));
                        g.fillRect(x, y + i * 2, 26, 1);
                        g.setColor(Color.WHITE);
                        g.fillRect(x, y + i * 2 + 1, 26, 1);
                    }
                    g.setColor(new Color(0x3C3B6E));
                    g.fillRect(x, y, 11, 10);
                    break;
                case "Pays de Galles":
                    g.setColor(Color.WHITE);
                    g.fillRect(x, y, 26, 9);
                    g.setColor(new Color(0x00A3A1));
                    g.fillRect(x, y + 9, 26, 9);
                    g.setColor(new Color(0xCE1124));
                    g.fillOval(x + 9, y + 5, 8, 8);
                    break;
                default:
                    g.setColor(new Color(0xC0C0C0));
                    g.fillRect(x, y, 26, 18);
                    break;
            }

            g.setColor(new Color(0x808080));
            g.drawRect(x, y, 25, 17);
            g.dispose();
        }

        private void bandes(Graphics2D g, int x, int y,
                Color haut, Color milieu, Color bas, boolean bandeHauteDouble) {
            int hauteurHaut = bandeHauteDouble ? 9 : 6;
            int hauteurMilieu = bandeHauteDouble ? 4 : 6;
            g.setColor(haut);
            g.fillRect(x, y, 26, hauteurHaut);
            g.setColor(milieu);
            g.fillRect(x, y + hauteurHaut, 26, hauteurMilieu);
            g.setColor(bas);
            g.fillRect(x, y + hauteurHaut + hauteurMilieu, 26,
                    18 - hauteurHaut - hauteurMilieu);
        }
    }

    private void installerEcouteurs() {
        btnPlanifier.addActionListener(e -> planifierMatch());
        btnEnregistrerScore.addActionListener(e -> enregistrerScore());
        btnActualiser.addActionListener(e -> chargerToutesLesDonnees());
        tfRecherche.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                appliquerRecherche();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                appliquerRecherche();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                appliquerRecherche();
            }
        });

        tableMatchs.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onMatchSelectionChange();
            }
        });
    }

    public void chargerToutesLesDonnees() {
        chargerPays();
        chargerStades();
        chargerMatchs();
    }

    private void chargerPays() {
        String sel1 = (String) cbPays1.getSelectedItem();
        String sel2 = (String) cbPays2.getSelectedItem();

        cbPays1.removeAllItems();
        cbPays2.removeAllItems();

        String query = "SELECT nom_pays FROM pays ORDER BY nom_pays ASC";
        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String pays = rs.getString("nom_pays");
                cbPays1.addItem(pays);
                cbPays2.addItem(pays);
            }

            if (sel1 != null) {
                cbPays1.setSelectedItem(sel1);
            }
            if (sel2 != null) {
                cbPays2.setSelectedItem(sel2);
            } else if (cbPays2.getItemCount() > 1) {
                cbPays2.setSelectedIndex(1);
            }

        } catch (SQLException ex) {
            afficherErreur("Impossible de charger la liste des pays.", ex);
        }
    }

    private void chargerStades() {
        cbStade.removeAllItems();

        String query = "SELECT id_stade, nom_stade, ville, pays "
                + "FROM stades ORDER BY nom_stade ASC";
        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id_stade");
                String nom = rs.getString("nom_stade");
                String ville = rs.getString("ville");
                String pays = rs.getString("pays");
                cbStade.addItem(new StadeItem(id, nom, ville, pays));
            }

        } catch (SQLException ex) {
            afficherErreur("Impossible de charger la liste des stades.", ex);
        }
    }

    private void chargerMatchs() {
        tableModel.setRowCount(0);
        selectedMatchId = null;
        lblMatchSelectionne.setText("Sélectionnez un match dans le tableau.");
        btnEnregistrerScore.setEnabled(false);
        int matchsJoues = 0;

        String query = "SELECT m.id_match, m.nom_pays_1, m.pays_1_buts, m.pays_2_buts, "
                     + "       m.nom_pays_2, s.id_stade, s.nom_stade, s.ville, s.pays, m.date_match "
                     + "FROM matchs m "
                     + "JOIN stades s ON m.id_stade = s.id_stade "
                     + "ORDER BY m.date_match ASC, m.id_match ASC";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id_match");
                String pays1 = rs.getString("nom_pays_1");
                Object b1Obj = rs.getObject("pays_1_buts");
                Object b2Obj = rs.getObject("pays_2_buts");
                String pays2 = rs.getString("nom_pays_2");
                StadeItem stade = new StadeItem(rs.getInt("id_stade"),
                        rs.getString("nom_stade"), rs.getString("ville"),
                        rs.getString("pays"));
                Date date = rs.getDate("date_match");

                String score1 = (b1Obj != null) ? String.valueOf(rs.getInt("pays_1_buts")) : "-";
                String score2 = (b2Obj != null) ? String.valueOf(rs.getInt("pays_2_buts")) : "-";

                tableModel.addRow(new Object[]{id, pays1, score1, score2, pays2, stade, date});
                if (b1Obj != null && b2Obj != null) {
                    matchsJoues++;
                }
            }

            actualiserIndicateurs(matchsJoues);
            appliquerRecherche();
        } catch (SQLException ex) {
            afficherErreur("Impossible de charger la liste des matchs.", ex);
        }
    }

    private void actualiserIndicateurs(int matchsJoues) {
        int total = tableModel.getRowCount();
        int aJouer = total - matchsJoues;
        lblNombreMatchs.setText("<html><b>RENCONTRES</b>&nbsp;&nbsp;" + total + "</html>");
        lblMatchsJoues.setText("<html><b>SCORE SAISI</b>&nbsp;&nbsp;"
                + matchsJoues + "</html>");
        lblMatchsAvenir.setText("<html><b>À JOUER</b>&nbsp;&nbsp;" + aJouer + "</html>");
    }

    private void appliquerRecherche() {
        if (trieurMatchs == null || tfRecherche == null) {
            return;
        }

        String recherche = tfRecherche.getText().trim();
        if (recherche.isEmpty()) {
            trieurMatchs.setRowFilter(null);
        } else {
            trieurMatchs.setRowFilter(RowFilter.regexFilter(
                    "(?i)" + Pattern.quote(recherche)));
        }

        int visibles = trieurMatchs.getViewRowCount();
        int total = tableModel.getRowCount();
        lblResultatsRecherche.setText(visibles + " / " + total
                + (total == 1 ? " match" : " matchs"));
    }

    private void onMatchSelectionChange() {
        int ligneVue = tableMatchs.getSelectedRow();
        if (ligneVue == -1) {
            selectedMatchId = null;
            lblMatchSelectionne.setText("Sélectionnez un match dans le tableau.");
            btnEnregistrerScore.setEnabled(false);
            return;
        }

        int selectedRow = tableMatchs.convertRowIndexToModel(ligneVue);
        selectedMatchId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String pays1 = (String) tableModel.getValueAt(selectedRow, 1);
        String score1Str = tableModel.getValueAt(selectedRow, 2).toString();
        String score2Str = tableModel.getValueAt(selectedRow, 3).toString();
        String pays2 = (String) tableModel.getValueAt(selectedRow, 4);

        lblMatchSelectionne.setText(String.format("Match #%d : %s vs %s", selectedMatchId, pays1, pays2));

        if (!score1Str.equals("-")) {
            try {
                spButs1.setValue(Integer.parseInt(score1Str));
            } catch (NumberFormatException ignored) {
                spButs1.setValue(0);
            }
        } else {
            spButs1.setValue(0);
        }

        if (!score2Str.equals("-")) {
            try {
                spButs2.setValue(Integer.parseInt(score2Str));
            } catch (NumberFormatException ignored) {
                spButs2.setValue(0);
            }
        } else {
            spButs2.setValue(0);
        }

        btnEnregistrerScore.setEnabled(true);
    }

    private void planifierMatch() {
        String pays1 = (String) cbPays1.getSelectedItem();
        String pays2 = (String) cbPays2.getSelectedItem();
        StadeItem stade = (StadeItem) cbStade.getSelectedItem();
        String dateStr = tfDate.getText().trim();

        if (pays1 == null || pays2 == null) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez vous assurer que les équipes sont chargées.",
                    "Erreur de saisie", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validation métier : interdiction d'un match contre soi-même
        if (pays1.equals(pays2)) {
            JOptionPane.showMessageDialog(this,
                    "Un pays ne peut pas jouer contre lui-même (" + pays1 + ").",
                    "Erreur de validation", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (stade == null) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un stade.",
                    "Erreur de saisie", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (dateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez saisir une date au format YYYY-MM-DD.",
                    "Erreur de saisie", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Date dateMatch;
        try {
            dateMatch = Date.valueOf(dateStr);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    "Format de date invalide : '" + dateStr + "'. Format attendu : YYYY-MM-DD (ex: 2022-11-20).",
                    "Erreur de format de date", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO matchs (nom_pays_1, nom_pays_2, id_stade, date_match, pays_1_buts, pays_2_buts) "
                   + "VALUES (?, ?, ?, ?, NULL, NULL)";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, pays1);
            ps.setString(2, pays2);
            ps.setInt(3, stade.getIdStade());
            ps.setDate(4, dateMatch);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this,
                        "Le match " + pays1 + " vs " + pays2 + " a été planifié avec succès !",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                chargerMatchs();
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la planification du match : " + ex.getMessage(),
                    "Erreur Base de Données", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void enregistrerScore() {
        if (selectedMatchId == null) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un match dans la table.",
                    "Aucune sélection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int buts1 = (Integer) spButs1.getValue();
        int buts2 = (Integer) spButs2.getValue();

        if (buts1 < 0 || buts2 < 0) {
            JOptionPane.showMessageDialog(this,
                    "Les buts doivent être supérieurs ou égaux à 0.",
                    "Erreur de validation", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "UPDATE matchs SET pays_1_buts = ?, pays_2_buts = ? WHERE id_match = ?";

        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, buts1);
            ps.setInt(2, buts2);
            ps.setInt(3, selectedMatchId);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this,
                        "Score enregistré avec succès (" + buts1 + " - " + buts2 + ") !",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                chargerMatchs();
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'enregistrement du score : " + ex.getMessage(),
                    "Erreur Base de Données", JOptionPane.ERROR_MESSAGE);
        }
    }
}
