package ui;

import javax.swing.*;
import ui.arsene.PanelStadeEtPays;
import ui.mohamine.PanelMatchs;
import ui.martin.PanelClassement;

public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("Coupe du Monde de Football - Gestion");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane onglets = new JTabbedPane();
        onglets.addTab("1. Équipes & Stades", new PanelStadeEtPays());
        onglets.addTab("2. Matchs & Scores", new PanelMatchs());
        onglets.addTab("3. Classements", new PanelClassement());

        add(onglets);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
