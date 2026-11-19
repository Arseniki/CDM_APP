package ui.mohamine;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import ui.design.UiTheme;

public class PanelMatchs extends JPanel {

    public PanelMatchs() {
        // 1. Panneau racine standard
        setLayout(new BorderLayout(0, UiTheme.ESPACE_VERTICAL));
        setBackground(UiTheme.COULEUR_FOND);
        setBorder(javax.swing.BorderFactory.createEmptyBorder(
                UiTheme.MARGE_EXTERNE, UiTheme.MARGE_EXTERNE,
                UiTheme.MARGE_EXTERNE, UiTheme.MARGE_EXTERNE));

        // 2. Entete
        add(UiTheme.creerEntete("Matchs et Scores"), BorderLayout.NORTH);

        // 3. Contenu central (formulaire + tableau)
        add(creerContenu(), BorderLayout.CENTER);

        // 4. Barre d'actions
        add(creerBarreActions(), BorderLayout.SOUTH);

        chargerDonnees();
    }

    private JPanel creerContenu() {
        JPanel contenu = new JPanel(new BorderLayout(0, UiTheme.ESPACE_VERTICAL));
        contenu.setBackground(UiTheme.COULEUR_FOND);

        // Section formulaire
        JPanel sectionForm = UiTheme.creerSection("Planifier un match");
        JPanel form = new JPanel(new java.awt.GridLayout(2, 4, UiTheme.MARGE_INTERNE, UiTheme.MARGE_INTERNE));
        form.setBackground(UiTheme.COULEUR_FOND);
        // ... ajouter labels et champs ici ...
        sectionForm.add(form, BorderLayout.CENTER);
        contenu.add(sectionForm, BorderLayout.NORTH);

        // Section tableau
        JPanel sectionTable = UiTheme.creerSection("Liste des matchs");
        tableMatchs = new javax.swing.JTable();
        sectionTable.add(UiTheme.creerScrollTable(tableMatchs), BorderLayout.CENTER);
        contenu.add(sectionTable, BorderLayout.CENTER);

        return contenu;
    }

    private JPanel creerBarreActions() {
        JPanel barre = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        barre.setBackground(UiTheme.COULEUR_FOND);
        barre.add(UiTheme.creerBouton("Actualiser"));
        return barre;
    }

    private void chargerDonnees() {
        // ...
    }
}