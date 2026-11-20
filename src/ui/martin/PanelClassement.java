package ui.martin;

import db.ConnexionDB;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import ui.design.UiTheme;

public class PanelClassement extends JPanel {
    private final JComboBox<String> filtre;
    private final DefaultTableModel modele;
    private final JLabel matchsJouesValeur;
    private final JLabel butsValeur;
    private final JLabel leaderValeur;

    public PanelClassement() {
        super(new BorderLayout());
        setBackground(UiTheme.COULEUR_FOND);

        JPanel racine = UiTheme.creerPanneauRacine();
        racine.add(UiTheme.creerEntete("Classements"), BorderLayout.NORTH);

        JPanel section = UiTheme.creerSection("Classement des équipes");
        JPanel contenu = new JPanel(new BorderLayout(0, UiTheme.ESPACE_VERTICAL));
        contenu.setBackground(UiTheme.COULEUR_FOND);

        JPanel barreActions = new JPanel(
                new FlowLayout(FlowLayout.LEFT, UiTheme.MARGE_INTERNE, 0));
        barreActions.setBackground(UiTheme.COULEUR_FOND);

        JLabel labelFiltre = new JLabel("Afficher");
        labelFiltre.setFont(UiTheme.POLICE_NORMALE);
        labelFiltre.setForeground(UiTheme.COULEUR_SECTION);
        filtre = new JComboBox<>(new String[]{
                "Groupe A", "Groupe B", "Classement Général"
        });
        filtre.setFont(UiTheme.POLICE_NORMALE);

        JButton boutonActualiser = UiTheme.creerBouton("Actualiser");
        boutonActualiser.addActionListener(event -> chargerClassement());
        filtre.addActionListener(event -> chargerClassement());

        barreActions.add(labelFiltre);
        barreActions.add(filtre);
        barreActions.add(boutonActualiser);

        JPanel cartes = new JPanel(new GridLayout(1, 3, UiTheme.MARGE_INTERNE, 0));
        cartes.setBackground(UiTheme.COULEUR_FOND);
        matchsJouesValeur = creerValeurCarte();
        butsValeur = creerValeurCarte();
        leaderValeur = creerValeurCarte();
        cartes.add(creerCarte("MATCHS JOUÉS", matchsJouesValeur));
        cartes.add(creerCarte("BUTS INSCRITS", butsValeur));
        cartes.add(creerCarte("EN TÊTE", leaderValeur));

        JPanel haut = new JPanel(new BorderLayout(0, UiTheme.ESPACE_VERTICAL));
        haut.setBackground(UiTheme.COULEUR_FOND);
        haut.add(barreActions, BorderLayout.NORTH);
        haut.add(cartes, BorderLayout.CENTER);
        contenu.add(haut, BorderLayout.NORTH);

        modele = new DefaultTableModel(
                new Object[]{"Rang", "Équipe", "Groupe", "MJ", "G", "N", "P",
                        "BP", "BC", "Diff", "Pts"}, 0) {
            @Override
            public boolean isCellEditable(int ligne, int colonne) {
                return false;
            }
        };
        JTable table = new JTable(modele);
        table.setDefaultRenderer(Object.class, new RenduClassement());
        table.getColumnModel().getColumn(0).setMaxWidth(65);
        table.getColumnModel().getColumn(1).setPreferredWidth(190);
        table.getColumnModel().getColumn(2).setMaxWidth(80);
        table.getColumnModel().getColumn(10).setMaxWidth(70);
        contenu.add(UiTheme.creerScrollTable(table), BorderLayout.CENTER);
        table.setRowHeight(UiTheme.HAUTEUR_LIGNE_TABLE + 4);
        section.add(contenu, BorderLayout.CENTER);

        racine.add(section, BorderLayout.CENTER);
        add(racine, BorderLayout.CENTER);
        chargerClassement();
    }

    private JPanel creerCarte(String titre, JLabel valeur) {
        JPanel carte = new JPanel(new BorderLayout(0, 4));
        carte.setBackground(Color.WHITE);
        carte.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(UiTheme.COULEUR_BORDURE),
                javax.swing.BorderFactory.createEmptyBorder(
                        UiTheme.MARGE_INTERNE, UiTheme.MARGE_EXTERNE,
                        UiTheme.MARGE_INTERNE, UiTheme.MARGE_EXTERNE)));

        JLabel entete = new JLabel(titre);
        entete.setFont(UiTheme.POLICE_SECTION);
        entete.setForeground(UiTheme.COULEUR_SECTION);
        carte.add(entete, BorderLayout.NORTH);
        carte.add(valeur, BorderLayout.CENTER);
        return carte;
    }

    private JLabel creerValeurCarte() {
        JLabel valeur = new JLabel("—", SwingConstants.LEFT);
        valeur.setFont(UiTheme.POLICE_TITRE);
        valeur.setForeground(UiTheme.COULEUR_ENTETE);
        return valeur;
    }

    private void chargerClassement() {
        modele.setRowCount(0);
        Map<String, Statistiques> statistiques = new HashMap<>();

        String choix = (String) filtre.getSelectedItem();
        String filtreGroupe = "Groupe A".equals(choix) ? "A"
                : "Groupe B".equals(choix) ? "B" : null;

        try (Connection connexion = ConnexionDB.getConnection();
             Statement requete = connexion.createStatement()) {

            String sqlPays = "SELECT nom_pays, groupe FROM pays";
            if (filtreGroupe != null) {
                sqlPays += " WHERE groupe = '" + filtreGroupe + "'";
            }

            try (ResultSet resultat = requete.executeQuery(sqlPays)) {
                while (resultat.next()) {
                    String equipe = resultat.getString("nom_pays");
                    statistiques.put(equipe, new Statistiques(
                            equipe, resultat.getString("groupe")));
                }
            }

            String sqlMatchs =
                    "SELECT nom_pays_1, nom_pays_2, pays_1_buts, pays_2_buts "
                            + "FROM matchs "
                            + "WHERE pays_1_buts IS NOT NULL "
                            + "AND pays_2_buts IS NOT NULL";

            try (ResultSet resultat = requete.executeQuery(sqlMatchs)) {
                while (resultat.next()) {
                    String equipe1 = resultat.getString("nom_pays_1");
                    String equipe2 = resultat.getString("nom_pays_2");
                    int buts1 = resultat.getInt("pays_1_buts");
                    int buts2 = resultat.getInt("pays_2_buts");

                    Statistiques stats1 = statistiques.get(equipe1);
                    Statistiques stats2 = statistiques.get(equipe2);
                    appliquerMatch(stats1, stats2, buts1, buts2);
                }
            }

            List<Statistiques> classement = new ArrayList<>(statistiques.values());
            classement.sort(Comparator
                    .comparingInt(Statistiques::points).reversed()
                    .thenComparing(Comparator.comparingInt(
                            Statistiques::difference).reversed())
                    .thenComparing(Statistiques::nomEquipe));

            int nombreMatchs = 0;
            int nombreButs = 0;
            int rang = 1;
            for (Statistiques stats : classement) {
                modele.addRow(new Object[]{
                        rang++,
                        stats.nomEquipe,
                        stats.groupe,
                        stats.matchsJoues,
                        stats.victoires,
                        stats.nuls,
                        stats.defaites,
                        stats.butsPour,
                        stats.butsContre,
                        stats.difference(),
                        stats.points()
                });
                nombreMatchs += stats.matchsJoues;
                nombreButs += stats.butsPour;
            }
            matchsJouesValeur.setText(Integer.toString(nombreMatchs / 2));
            butsValeur.setText(Integer.toString(nombreButs));
            leaderValeur.setText(classement.isEmpty()
                    ? "—" : classement.get(0).nomEquipe);
            leaderValeur.setIcon(classement.isEmpty()
                    ? null : new DrapeauPays(classement.get(0).nomEquipe));
            leaderValeur.setHorizontalTextPosition(SwingConstants.RIGHT);
            leaderValeur.setIconTextGap(UiTheme.MARGE_INTERNE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Impossible de charger le classement.\n" + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void appliquerMatch(Statistiques equipe1, Statistiques equipe2,
                               int buts1, int buts2) {
        if (equipe1 == null || equipe2 == null) {
            return;
        }

        equipe1.matchsJoues++;
        equipe2.matchsJoues++;
        equipe1.butsPour += buts1;
        equipe1.butsContre += buts2;
        equipe2.butsPour += buts2;
        equipe2.butsContre += buts1;

        if (buts1 > buts2) {
            equipe1.victoires++;
            equipe2.defaites++;
        } else if (buts1 < buts2) {
            equipe2.victoires++;
            equipe1.defaites++;
        } else {
            equipe1.nuls++;
            equipe2.nuls++;
        }
    }

    private static final class Statistiques {
        private final String nomEquipe;
        private final String groupe;
        private int matchsJoues;
        private int victoires;
        private int nuls;
        private int defaites;
        private int butsPour;
        private int butsContre;

        private Statistiques(String nomEquipe, String groupe) {
            this.nomEquipe = nomEquipe;
            this.groupe = groupe;
        }

        private int difference() {
            return butsPour - butsContre;
        }

        private int points() {
            return victoires * 3 + nuls;
        }

        private String nomEquipe() {
            return nomEquipe;
        }
    }

    private static final class RenduClassement extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object valeur, boolean selectionne,
                boolean focus, int ligne, int colonne) {
            Component composant = super.getTableCellRendererComponent(
                    table, valeur, selectionne, focus, ligne, colonne);
            int rang = (int) table.getValueAt(ligne, 0);
            setIcon(null);
            setHorizontalAlignment(colonne == 1
                    ? SwingConstants.LEFT : SwingConstants.CENTER);

            if (!selectionne) {
                setBackground(ligne % 2 == 0
                        ? Color.WHITE : UiTheme.COULEUR_TABLE_ALT);
                setForeground(Color.BLACK);
                if (colonne == 0 && rang == 1) {
                    setBackground(UiTheme.COULEUR_BOUTON);
                    setForeground(UiTheme.COULEUR_BOUTON_TEXTE);
                }
            }

            if (colonne == 1 && valeur != null) {
                setIcon(new DrapeauPays(valeur.toString()));
                setIconTextGap(UiTheme.MARGE_INTERNE);
            }
            if (!selectionne && colonne == 10) {
                setFont(UiTheme.POLICE_SECTION);
                if (valeur instanceof Number && ((Number) valeur).intValue() > 0) {
                    setForeground(UiTheme.COULEUR_ENTETE);
                }
            } else {
                setFont(UiTheme.POLICE_TABLE);
            }
            return composant;
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
        public void paintIcon(Component composant, java.awt.Graphics graphics, int x, int y) {
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
            int hauteurAutre = bandeHauteDouble ? 4 : 6;
            g.setColor(haut);
            g.fillRect(x, y, 26, hauteurHaut);
            g.setColor(milieu);
            g.fillRect(x, y + hauteurHaut, 26, hauteurAutre);
            g.setColor(bas);
            g.fillRect(x, y + hauteurHaut + hauteurAutre, 26,
                    18 - hauteurHaut - hauteurAutre);
        }
    }
}
