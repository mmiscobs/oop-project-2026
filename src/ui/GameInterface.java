package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.io.IOException;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import simulation.Game;
import simulation.GameDifficulty;
import utils.Reactive;

public class GameInterface extends JPanel {
    private static final int FRAME_W = 2100;
    private static final int FRAME_H = 1400;

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("City Map Demo");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            GameInterface root = new GameInterface();

            f.setJMenuBar(root.createMenuBar());
            f.setContentPane(root);
            f.setSize(FRAME_W, FRAME_H);
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }

    public Game game = new Game();

    GameInterface() {
        super(new BorderLayout());
        game.simulator.subscribe(s -> this.render());
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu saveMenu = new JMenu("Save");
        JMenuItem saveAction = new JMenuItem("Save game");
        JMenuItem saveAndExitAction = new JMenuItem("Save and exit to menu");
        JMenuItem exitAction = new JMenuItem("Exit to menu");
        saveAction.addActionListener(e -> {
            try {
                this.game.createSave();
            } catch (IOException err) {
                System.err.print(err);
            }
        });
        exitAction.addActionListener(e -> this.game.simulator.set(null));
        saveAndExitAction.addActionListener(e -> {
            try {
                this.game.createSave();
            } catch (IOException err) {
                System.err.print(err);
            }
            this.game.simulator.set(null);
        });
        this.game.simulator.subscribe(s -> {
            saveAction.setEnabled(s != null);
            saveAndExitAction.setEnabled(s != null);
            exitAction.setEnabled(s != null);
        });
        saveMenu.add(saveAction);
        saveMenu.add(saveAndExitAction);
        saveMenu.add(exitAction);
        menuBar.add(saveMenu);
        return menuBar;
    }

    private void render() {
        this.removeAll();
        if (game.simulator.get() != null) {
            JPanel simulationUI = new SimulationInterface(game.simulator.get());

            this.add(simulationUI);
        } else {
            this.add(new MainMenu(), BorderLayout.CENTER);
        }
        this.revalidate();
        this.repaint();
    }

    class MainMenu extends JPanel {
        private Reactive<JPanel> currentSubpage = new Reactive<>(null);

        MainMenu() {
            super();
            currentSubpage.subscribe(p -> this.render());
            game.simulator.subscribe(s -> this.render());
        }

        private void render() {
            this.removeAll();
            if (currentSubpage.get() == null) {
                this.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
                this.add(new NewGameButton());
                this.add(new LoadGameButton());
                this.add(new CreditsButton());
                this.add(new Exit());
            } else {
                this.setLayout(new BorderLayout());
                this.add(new BackButton(), BorderLayout.NORTH);
                this.add(currentSubpage.get(), BorderLayout.CENTER);
            }
            this.revalidate();
            this.repaint();
        }

        class BackButton extends JButton {
            BackButton() {
                super("Back");
                this.addActionListener(e -> currentSubpage.set(null));
            }
        }

        class NewGameButton extends JButton {
            NewGameButton() {
                super("New Game");
                this.addActionListener(e -> currentSubpage.set(new NewGame()));
            }
        }

        class NewGame extends JPanel {
            NewGame() {
                super(new FlowLayout());
                JPanel container = new JPanel(new GridLayout(0, 1, 10, 10));
                this.add(container);
                container.setMaximumSize(new Dimension(300, 500));
                container.add(new Label("Size of map"));
                JSlider size = new JSlider(JSlider.HORIZONTAL, 10, 20, 10);
                container.add(size);
                container.add(new Label("City name"));
                JTextField cityName = new JTextField("My New Town");
                container.add(cityName);
                JComboBox<GameDifficulty> combo = new JComboBox<>(GameDifficulty.values());
                container.add(combo);

                JButton submitButton = new JButton("Start a new game");
                onChange(cityName, v -> submitButton.setEnabled(v.trim().length() > 0 && !v.contains("/")));
                submitButton.addActionListener(e -> {
                    game.startNewSimulation(size.getValue(), size.getValue(), (GameDifficulty) combo.getSelectedItem(),
                            cityName.getText());
                });
                container.add(submitButton);
            }

            public static void onChange(JTextField field, Consumer<String> handler) {
                field.getDocument().addDocumentListener(new DocumentListener() {
                    public void insertUpdate(DocumentEvent e) {
                        handler.accept(field.getText());
                    }

                    public void removeUpdate(DocumentEvent e) {
                        handler.accept(field.getText());
                    }

                    public void changedUpdate(DocumentEvent e) {
                        handler.accept(field.getText());
                    }
                });
            }
        }

        class LoadGameButton extends JButton {
            LoadGameButton() {
                super("Load Game");
                this.addActionListener(e -> currentSubpage.set(new LoadGame()));
            }
        }

        class LoadGame extends JPanel {
            LoadGame() {
                render();
            }

            private void render() {
                this.removeAll();
                for (String save : game.listSaves()) {
                    JButton loadSaveButton = new JButton(save);
                    loadSaveButton.addActionListener(e -> {
                        try {
                            game.loadSave(save);
                        } catch (IOException err) {
                            System.err.print(err);
                        }
                    });
                    this.add(loadSaveButton);
                }
                this.revalidate();
                this.repaint();
            }
        }

        class CreditsButton extends JButton {
            CreditsButton() {
                super("Credits");
                this.addActionListener(e -> currentSubpage.set(new Credits()));
            }
        }

        class Credits extends JPanel {
            Credits() {
                this.add(new JLabel("Daniil Poliakov, Lia Bulghadaryan, Mariam Sargsyan 2025 (C)"));
            }
        }

        class Exit extends JButton {
            Exit() {
                super("Exit");
                this.addActionListener(e -> System.exit(0));
            }
        }
    }
}
