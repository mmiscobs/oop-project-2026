package ui;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import simulation.Game;
import simulation.GameDifficulty;

public class GameInterface extends JPanel {
    private static final int FRAME_W = 2100;
    private static final int FRAME_H = 1400;
    private static final int COLS = 15;
    private static final int ROWS = 15;

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("City Map Demo");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel root = new GameInterface();

            f.setContentPane(root);
            f.setSize(FRAME_W, FRAME_H);
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }

    GameInterface() {
        super(new BorderLayout());
        Game game = new Game();

        game.startNewSimulation(COLS, ROWS, GameDifficulty.MEDIUM);

        JPanel simulationUI = new SimulationInterface(game.simulator);

        this.add(simulationUI);
    }
}
