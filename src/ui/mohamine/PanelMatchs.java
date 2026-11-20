package ui.mohamine;

import db.ConnexionDB;
import ui.design.UiTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

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

        public StadeItem(int idStade, String nomStade, String ville) {
            this.idStade = idStade;
            this.nomStade = nomStade;
            this.ville = ville;
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
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cbPays1 = new JComboBox<>();
        cbPays2 = new JComboBox<>();
        cbStade = new JComboBox<>();
        tfDate = new JTextField("2022-11-20", 10);
        btnPlanifier = UiTheme.creerBouton("Planifier le match");

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
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
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

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tableMatchs.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableMatchs.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tableMatchs.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        tableMatchs.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        tableMatchs.getColumnModel().getColumn(0).setPreferredWidth(45);
        tableMatchs.getColumnModel().getColumn(0).setMaxWidth(60);

        section.add(UiTheme.creerScrollTable(tableMatchs), BorderLayout.CENTER);
        return section;
    }

    private JPanel creerSectionScores() {
        JPanel section = UiTheme.creerSection("3. Saisie / Modification du score");

        JPanel formScore = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 6));
        formScore.setBackground(UiTheme.COULEUR_FOND);

        lblMatchSelectionne = new JLabel("Sélectionnez un match dans le tableau.");
        lblMatchSelectionne.setFont(UiTheme.POLICE_SECTION);

        spButs1 = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
        spButs2 = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
        spButs1.setPreferredSize(new Dimension(50, 24));
        spButs2.setPreferredSize(new Dimension(50, 24));

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
        return label;
    }

    private void installerEcouteurs() {
        btnPlanifier.addActionListener(e -> planifierMatch());
        btnEnregistrerScore.addActionListener(e -> enregistrerScore());
        btnActualiser.addActionListener(e -> chargerToutesLesDonnees());

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
            System.err.println("Erreur chargement pays : " + ex.getMessage());
        }
    }

    private void chargerStades() {
        cbStade.removeAllItems();

        String query = "SELECT id_stade, nom_stade, ville FROM stades ORDER BY nom_stade ASC";
        try (Connection conn = ConnexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id_stade");
                String nom = rs.getString("nom_stade");
                String ville = rs.getString("ville");
                cbStade.addItem(new StadeItem(id, nom, ville));
            }

        } catch (SQLException ex) {
            System.err.println("Erreur chargement stades : " + ex.getMessage());
        }
    }

    private void chargerMatchs() {
        tableModel.setRowCount(0);
        selectedMatchId = null;
        lblMatchSelectionne.setText("Sélectionnez un match dans le tableau.");
        btnEnregistrerScore.setEnabled(false);

        String query = "SELECT m.id_match, m.nom_pays_1, m.pays_1_buts, m.pays_2_buts, "
                     + "       m.nom_pays_2, s.nom_stade, m.date_match "
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
                String stade = rs.getString("nom_stade");
                Date date = rs.getDate("date_match");

                String score1 = (b1Obj != null) ? String.valueOf(rs.getInt("pays_1_buts")) : "-";
                String score2 = (b2Obj != null) ? String.valueOf(rs.getInt("pays_2_buts")) : "-";

                tableModel.addRow(new Object[]{id, pays1, score1, score2, pays2, stade, date});
            }

        } catch (SQLException ex) {
            System.err.println("Erreur chargement matchs : " + ex.getMessage());
        }
    }

    private void onMatchSelectionChange() {
        int selectedRow = tableMatchs.getSelectedRow();
        if (selectedRow == -1) {
            selectedMatchId = null;
            lblMatchSelectionne.setText("Sélectionnez un match dans le tableau.");
            btnEnregistrerScore.setEnabled(false);
            return;
        }

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
