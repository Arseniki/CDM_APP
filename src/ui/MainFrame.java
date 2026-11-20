package ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import ui.arsene.PanelStadeEtPays;
import ui.design.UiTheme;
import ui.martin.PanelClassement;
import ui.mohamine.PanelMatchs;

public class MainFrame extends JFrame {
    private static final String[] NOMS_ONGLETS = {
        "Équipes & Stades", "Matchs & Scores", "Classements"
    };
    private static final String[] DESCRIPTIONS_ONGLETS = {
        "Gérer les équipes et les stades",
        "Planifier les rencontres et saisir les scores",
        "Consulter les statistiques des groupes"
    };

    private final CardLayout dispositionModules;
    private final JPanel contenuModules;
    private final JToggleButton[] boutonsModules;
    private final JLabel statut;
    private int moduleActif;

    public MainFrame() {
        super("Coupe du Monde 2022");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(900, 600));
        setSize(1100, 720);
        setLocationRelativeTo(null);

        JPanel racine = new JPanel(new BorderLayout());
        racine.setBackground(UiTheme.COULEUR_FOND);
        racine.add(creerBandeau(), BorderLayout.NORTH);

        boutonsModules = new JToggleButton[NOMS_ONGLETS.length];
        JPanel navigation = creerNavigation();
        contenuModules = new JPanel();
        dispositionModules = new CardLayout();
        contenuModules.setLayout(dispositionModules);
        contenuModules.setBackground(UiTheme.COULEUR_FOND);
        contenuModules.add(new PanelStadeEtPays(), "module-0");
        contenuModules.add(new PanelMatchs(), "module-1");
        contenuModules.add(new PanelClassement(), "module-2");

        JPanel centre = new JPanel(new BorderLayout(0, UiTheme.ESPACE_VERTICAL));
        centre.setBackground(UiTheme.COULEUR_FOND);
        centre.setBorder(BorderFactory.createEmptyBorder(
                UiTheme.MARGE_INTERNE, UiTheme.MARGE_EXTERNE,
                UiTheme.MARGE_INTERNE, UiTheme.MARGE_EXTERNE));
        centre.add(navigation, BorderLayout.NORTH);
        centre.add(contenuModules, BorderLayout.CENTER);
        racine.add(centre, BorderLayout.CENTER);

        statut = new JLabel();
        JPanel pied = creerPied();
        racine.add(pied, BorderLayout.SOUTH);

        installerNavigation();
        afficherModule(0);
        setContentPane(racine);
        installerRaccourcis(racine);
    }

    private JPanel creerNavigation() {
        JPanel navigation = new JPanel(new GridLayout(
                1, NOMS_ONGLETS.length, UiTheme.MARGE_INTERNE, 0));
        navigation.setBackground(UiTheme.COULEUR_FOND);

        ButtonGroup groupe = new ButtonGroup();
        for (int i = 0; i < NOMS_ONGLETS.length; i++) {
            JToggleButton bouton = new JToggleButton(
                    "<html><b>" + NOMS_ONGLETS[i] + "</b><br>"
                            + "<font size='3'>" + DESCRIPTIONS_ONGLETS[i] + "</font></html>",
                    new NumeroModuleIcon(i + 1));
            bouton.setFont(UiTheme.POLICE_NORMALE);
            bouton.setHorizontalAlignment(SwingConstants.LEFT);
            bouton.setHorizontalTextPosition(SwingConstants.RIGHT);
            bouton.setIconTextGap(UiTheme.MARGE_INTERNE);
            bouton.setFocusPainted(false);
            bouton.setOpaque(true);
            bouton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UiTheme.COULEUR_BORDURE),
                    BorderFactory.createEmptyBorder(
                            UiTheme.MARGE_INTERNE, UiTheme.MARGE_EXTERNE,
                            UiTheme.MARGE_INTERNE, UiTheme.MARGE_EXTERNE)));
            bouton.setToolTipText("Ouvrir " + NOMS_ONGLETS[i]
                    + " (Ctrl+" + (i + 1) + ")");
            boutonsModules[i] = bouton;
            groupe.add(bouton);
            navigation.add(bouton);
        }
        return navigation;
    }

    private void installerNavigation() {
        for (int i = 0; i < boutonsModules.length; i++) {
            final int index = i;
            boutonsModules[i].addActionListener(event -> afficherModule(index));
        }
    }

    private void afficherModule(int index) {
        moduleActif = index;
        dispositionModules.show(contenuModules, "module-" + index);
        for (int i = 0; i < boutonsModules.length; i++) {
            boolean actif = i == index;
            boutonsModules[i].setSelected(actif);
            boutonsModules[i].setBackground(actif
                    ? UiTheme.COULEUR_ENTETE : UiTheme.COULEUR_TEXTE_ENTETE);
            boutonsModules[i].setForeground(actif
                    ? UiTheme.COULEUR_TEXTE_ENTETE : UiTheme.COULEUR_SECTION);
            boutonsModules[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(actif
                            ? UiTheme.COULEUR_ENTETE : UiTheme.COULEUR_BORDURE,
                            actif ? 2 : 1),
                    BorderFactory.createEmptyBorder(
                            UiTheme.MARGE_INTERNE, UiTheme.MARGE_EXTERNE,
                            UiTheme.MARGE_INTERNE, UiTheme.MARGE_EXTERNE)));
        }
        statut.setText("  ●   Module actif : " + NOMS_ONGLETS[index]);
        revalidate();
        repaint();
    }

    private JPanel creerBandeau() {
        JPanel bandeau = new JPanel(new BorderLayout(
                UiTheme.MARGE_EXTERNE, 0));
        bandeau.setBackground(UiTheme.COULEUR_ENTETE);
        bandeau.setBorder(new EmptyBorder(
                UiTheme.MARGE_EXTERNE, UiTheme.MARGE_EXTERNE,
                UiTheme.MARGE_EXTERNE, UiTheme.MARGE_EXTERNE));

        JLabel ballon = new JLabel(new BallonIcon());
        ballon.setBorder(new EmptyBorder(0, 0, 0, UiTheme.MARGE_INTERNE));
        bandeau.add(ballon, BorderLayout.WEST);

        JPanel titres = new JPanel(new BorderLayout(0, 4));
        titres.setOpaque(false);
        JLabel titre = new JLabel("COUPE DU MONDE");
        titre.setFont(UiTheme.POLICE_TITRE);
        titre.setForeground(UiTheme.COULEUR_TEXTE_ENTETE);

        JLabel sousTitre = new JLabel("QATAR 2022  ·  CENTRE DE GESTION");
        sousTitre.setFont(UiTheme.POLICE_NORMALE);
        sousTitre.setForeground(UiTheme.COULEUR_TABLE_ALT);
        titres.add(titre, BorderLayout.NORTH);
        titres.add(sousTitre, BorderLayout.SOUTH);
        bandeau.add(titres, BorderLayout.CENTER);

        JLabel edition = new JLabel("FIFA  |  22e ÉDITION", SwingConstants.RIGHT);
        edition.setFont(UiTheme.POLICE_SECTION);
        edition.setForeground(UiTheme.COULEUR_TEXTE_ENTETE);
        edition.setBorder(new EmptyBorder(0, UiTheme.MARGE_INTERNE, 0, 0));
        bandeau.add(edition, BorderLayout.EAST);
        return bandeau;
    }

    private JPanel creerPied() {
        JPanel pied = new JPanel(new BorderLayout());
        pied.setBackground(UiTheme.COULEUR_TEXTE_ENTETE);
        pied.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(
                        1, 0, 0, 0, UiTheme.COULEUR_BORDURE),
                new EmptyBorder(UiTheme.MARGE_INTERNE,
                        UiTheme.MARGE_EXTERNE, UiTheme.MARGE_INTERNE,
                        UiTheme.MARGE_EXTERNE)));

        statut.setFont(UiTheme.POLICE_NORMALE);
        statut.setForeground(UiTheme.COULEUR_SECTION);
        pied.add(statut, BorderLayout.WEST);

        JLabel raccourcis = new JLabel("RACCOURCIS   Ctrl+1  Équipes   ·   Ctrl+2  Matchs   ·   Ctrl+3  Classements");
        raccourcis.setFont(UiTheme.POLICE_TABLE);
        raccourcis.setForeground(UiTheme.COULEUR_SECTION);
        pied.add(raccourcis, BorderLayout.EAST);
        return pied;
    }

    private void installerRaccourcis(JPanel racine) {
        for (int i = 0; i < NOMS_ONGLETS.length; i++) {
            final int index = i;
            String action = "ouvrir-module-" + (i + 1);
            AbstractAction ouvrirModule = new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent event) {
                    afficherModule(index);
                }
            };

            int[] touches = {KeyEvent.VK_1 + i, KeyEvent.VK_NUMPAD1 + i};
            for (int touche : touches) {
                KeyStroke raccourci = KeyStroke.getKeyStroke(
                        touche, InputEvent.CTRL_DOWN_MASK);
                getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                        .put(raccourci, action);
                racine.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                        .put(raccourci, action);
            }
            getRootPane().getActionMap().put(action, ouvrirModule);
            racine.getActionMap().put(action, ouvrirModule);
        }
    }

    private static final class NumeroModuleIcon implements Icon {
        private final int numero;

        private NumeroModuleIcon(int numero) {
            this.numero = numero;
        }

        @Override
        public int getIconWidth() {
            return 36;
        }

        @Override
        public int getIconHeight() {
            return 36;
        }

        @Override
        public void paintIcon(Component composant, Graphics graphics, int x, int y) {
            boolean actif = composant instanceof JToggleButton
                    && ((JToggleButton) composant).isSelected();
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(actif ? UiTheme.COULEUR_BOUTON
                    : UiTheme.COULEUR_TABLE_ALT);
            g.fillOval(x, y, 36, 36);
            g.setColor(actif ? UiTheme.COULEUR_BOUTON_TEXTE
                    : UiTheme.COULEUR_ENTETE);
            g.setFont(UiTheme.POLICE_SECTION);
            String texte = Integer.toString(numero);
            int largeur = g.getFontMetrics().stringWidth(texte);
            int hauteur = g.getFontMetrics().getAscent();
            g.drawString(texte, x + (36 - largeur) / 2,
                    y + (36 - g.getFontMetrics().getHeight()) / 2 + hauteur);
            g.dispose();
        }
    }

    private static final class BallonIcon implements Icon {
        @Override
        public int getIconWidth() {
            return 54;
        }

        @Override
        public int getIconHeight() {
            return 54;
        }

        @Override
        public void paintIcon(Component composant, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(UiTheme.COULEUR_BOUTON);
            g.fillOval(x, y, 54, 54);
            g.setColor(UiTheme.COULEUR_TEXTE_ENTETE);
            g.fillOval(x + 7, y + 7, 40, 40);
            g.setColor(UiTheme.COULEUR_ENTETE);
            g.fillPolygon(
                    new int[]{x + 27, x + 34, x + 31, x + 23, x + 20},
                    new int[]{y + 14, y + 20, y + 29, y + 29, y + 20}, 5);
            g.fillPolygon(
                    new int[]{x + 11, x + 18, x + 20, x + 16, x + 9},
                    new int[]{y + 24, y + 25, y + 33, y + 39, y + 33}, 5);
            g.fillPolygon(
                    new int[]{x + 43, x + 36, x + 34, x + 38, x + 45},
                    new int[]{y + 24, y + 25, y + 33, y + 39, y + 33}, 5);
            g.fillPolygon(
                    new int[]{x + 19, x + 24, x + 31, x + 34, x + 27},
                    new int[]{y + 43, y + 36, y + 36, y + 44, y + 49}, 5);
            g.dispose();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Impossible d'appliquer le thème Swing standard : "
                    + e.getMessage());
        }
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
