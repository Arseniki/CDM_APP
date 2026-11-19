package ui.design;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 * Theme visuel commun aux 3 modules de l'application.
 *
 * Regles d'utilisation (a respecter par tous) :
 *  - Ne jamais mettre de couleur en dur (new Color(...)) dans un panneau.
 *  - Ne jamais mettre de police en dur (new Font(...)) dans un panneau.
 *  - Toujours utiliser creerEntete / creerBouton / creerSection / creerScrollTable.
 *  - Ne pas modifier ce fichier sans l'accord de l'equipe.
 */
public final class UiTheme {

    private UiTheme() {
        // Classe utilitaire : pas d'instanciation
    }

    // ------------------------------------------------------------
    // Couleurs
    // ------------------------------------------------------------
    public static final Color COULEUR_FOND          = new Color(0xF5F5F5);
    public static final Color COULEUR_ENTETE        = new Color(0x1F3A5F);
    public static final Color COULEUR_TEXTE_ENTETE  = Color.WHITE;
    public static final Color COULEUR_BOUTON        = new Color(0x2E6DA4);
    public static final Color COULEUR_BOUTON_TEXTE  = Color.WHITE;
    public static final Color COULEUR_SECTION       = new Color(0x333333);
    public static final Color COULEUR_BORDURE       = new Color(0xC0C0C0);
    public static final Color COULEUR_GRILLE_TABLE  = new Color(0xD0D0D0);
    public static final Color COULEUR_TABLE_ALT     = new Color(0xEEF3F8);

    // ------------------------------------------------------------
    // Polices
    // ------------------------------------------------------------
    public static final Font POLICE_TITRE    = new Font("SansSerif", Font.BOLD, 18);
    public static final Font POLICE_SECTION  = new Font("SansSerif", Font.BOLD, 13);
    public static final Font POLICE_NORMALE  = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font POLICE_TABLE    = new Font("SansSerif", Font.PLAIN, 12);

    // ------------------------------------------------------------
    // Marges
    // ------------------------------------------------------------
    public static final int MARGE_EXTERNE   = 15;
    public static final int MARGE_INTERNE   = 8;
    public static final int ESPACE_VERTICAL = 10;
    public static final int HAUTEUR_LIGNE_TABLE = 24;

    // ------------------------------------------------------------
    // Helpers de construction
    // ------------------------------------------------------------

    /**
     * Entete de panneau : bandeau fonce avec un titre en blanc.
     * A placer en BorderLayout.NORTH du panneau principal.
     */
    public static JPanel creerEntete(String titre) {
        JPanel panneau = new JPanel(new BorderLayout());
        panneau.setBackground(COULEUR_ENTETE);
        panneau.setBorder(BorderFactory.createEmptyBorder(
                MARGE_INTERNE, MARGE_EXTERNE, MARGE_INTERNE, MARGE_EXTERNE));

        JLabel label = new JLabel(titre);
        label.setFont(POLICE_TITRE);
        label.setForeground(COULEUR_TEXTE_ENTETE);
        panneau.add(label, BorderLayout.WEST);

        return panneau;
    }

    /**
     * Bouton d'action standard (fond bleu, texte blanc).
     */
    public static JButton creerBouton(String texte) {
        JButton bouton = new JButton(texte);
        bouton.setBackground(COULEUR_BOUTON);
        bouton.setForeground(COULEUR_BOUTON_TEXTE);
        bouton.setFont(POLICE_NORMALE);
        bouton.setFocusPainted(false);
        return bouton;
    }

    /**
     * Section titree : un JPanel vide avec un titre en haut.
     * Le developpeur ajoute son contenu dans le CENTER du panneau retourne.
     */
    public static JPanel creerSection(String titre) {
        JPanel panneau = new JPanel(new BorderLayout(0, MARGE_INTERNE));
        panneau.setBackground(COULEUR_FOND);

        JLabel label = new JLabel(titre);
        label.setFont(POLICE_SECTION);
        label.setForeground(COULEUR_SECTION);
        panneau.add(label, BorderLayout.NORTH);

        return panneau;
    }

    /**
     * Scroll pane standard pour une JTable (police, hauteur de ligne, grille).
     */
    public static JScrollPane creerScrollTable(JTable table) {
        table.setFont(POLICE_TABLE);
        table.setRowHeight(HAUTEUR_LIGNE_TABLE);
        table.setFillsViewportHeight(true);
        table.setGridColor(COULEUR_GRILLE_TABLE);
        table.getTableHeader().setFont(POLICE_SECTION);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(COULEUR_BORDURE));
        scroll.getViewport().setBackground(Color.WHITE);

        return scroll;
    }

    /**
     * Panneau racine standard : fond, marges externes, layout vertical.
     * A utiliser comme base de chaque PanelXxx.
     */
    public static JPanel creerPanneauRacine() {
        JPanel panneau = new JPanel(new BorderLayout(0, ESPACE_VERTICAL));
        panneau.setBackground(COULEUR_FOND);
        panneau.setBorder(BorderFactory.createEmptyBorder(
                MARGE_EXTERNE, MARGE_EXTERNE, MARGE_EXTERNE, MARGE_EXTERNE));
        return panneau;
    }
}