package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import buildings.Buildable;
import buildings.Buildable.UnregisteredBuildingType;
import buildings.privatebuilding.residential.ResidentialBuilding;
import buildings.privatebuilding.workplace.commercial.CommercialBuilding;
import buildings.privatebuilding.workplace.industrial.IndustrialBuilding;
import buildings.privatebuilding.workplace.office.OfficeBuilding;
import buildings.publicbuilding.service.healthcare.HealthcareBuilding;
import buildings.publicbuilding.service.police.PoliceStation;
import buildings.publicbuilding.transportation.PublicTransportation;
import city.City;
import simulation.GameSpeed;
import simulation.Simulator;
import utils.Point;

public class SimulationInterface {
    private static final int TILE_W = 34;
    private static final int TILE_H = 16;
    private static final int COLS = 15;
    private static final int ROWS = 15;
    private static final int FRAME_W = 2000;
    private static final int FRAME_H = 1400;

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(() -> {
            SimulationInterface ui = new SimulationInterface();
            ui.showWindow();
        });
    }

    private CityView view;
    private Simulator simulator;

    private void showWindow() {
        City city = new City(COLS, ROWS);
        this.simulator = new Simulator(city);

        this.view = new CityView(city, COLS, ROWS);

        view.render();
        final int ZOOM = 4;
        view.setOrigin(TILE_W * COLS / 2 * ZOOM, 2 * TILE_H * ZOOM);
        view.setZoom(ZOOM);

        JFrame f = new JFrame("City Map Demo");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());

        JPanel left = getBuildActionsPanel(city);
        JPanel bottom = new BottomPanel();

        root.add(left, BorderLayout.WEST);
        root.add(view, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        JMenuBar menuBar = new JMenuBar();
        f.setJMenuBar(menuBar);
        f.setContentPane(root);
        f.setSize(FRAME_W, FRAME_H);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private JPanel getBuildActionsPanel(City city) {
        ArrayList<Class<? extends Buildable>> BuildableClasses = getBuildableClassesSortedByType();

        ArrayList<TogglesMenu.Action> actions = new ArrayList<>();
        for (Class<? extends Buildable> BuildableClass : BuildableClasses) {
            actions.add(new TogglesMenu.Action() {
                private String getPrettyName() {
                    return BuildableClass.getSimpleName().replaceAll("[A-Z]", " $0").trim();
                }

                public String getName() {
                    return "Build " + getPrettyName();
                }

                public String getStopName() {
                    return "Stop building " + getPrettyName();
                }

                public Runnable enable(Runnable onEnd) {
                    if (PublicTransportation.class.isAssignableFrom(BuildableClass))
                        return CityView.enableManhattanDragAction(view,
                                (dragInfo) -> "<html><b>" + getPrettyName() + " preview</b><br/>"
                                        + CityView.manhattanPath(dragInfo.from, dragInfo.to).size()
                                        + " tiles &rarr; ("
                                        + dragInfo.to.x + ", " + dragInfo.to.y + ")</html>",
                                (tiles, cleanup) -> {
                                    try {
                                        for (Point point : tiles) {
                                            city.grid.placeBuildingAt(point, Buildable.createBuilding(BuildableClass));
                                        }
                                    } catch (UnregisteredBuildingType e) {
                                    }
                                    cleanup.run();
                                    onEnd.run();
                                    view.render();
                                });
                    try {
                        Buildable building = Buildable.createBuilding(BuildableClass);
                        return CityView.enableBuildAction(view, (loc) -> loc.x + " " + loc.y, building.getLength(),
                                building.getWidth(), (tile, cleanup) -> {
                                    city.grid.placeBuildingAt(tile, building);
                                    onEnd.run();
                                    view.render();
                                });
                    } catch (UnregisteredBuildingType e) {
                        System.err.print(e);
                        onEnd.run();
                        return () -> {
                        };
                    }
                }
            });
        }
        actions.add(new TogglesMenu.Action() {
            public String getName() {
                return "Demolish";
            };

            public String getStopName() {
                return "Stop demolishing";
            }

            public Runnable enable(Runnable onEnd) {
                return CityView.enableDemolishAction(view, (loc, cleanup) -> {
                    city.grid.removeBuildingAt(loc);

                    cleanup.run();
                    onEnd.run();
                    view.render();
                });
            }
        });
        JPanel left = new TogglesMenu("Build", actions);
        return left;
    }

    private ArrayList<Class<? extends Buildable>> getBuildableClassesSortedByType() {
        ArrayList<Class<? extends Buildable>> BuildableClasses = new ArrayList<>();

        for (Class<? extends Buildable> CategoryClass : List.of(
                PublicTransportation.class,
                ResidentialBuilding.class,
                CommercialBuilding.class,
                IndustrialBuilding.class,
                OfficeBuilding.class,
                HealthcareBuilding.class,
                PoliceStation.class))
            for (Class<? extends Buildable> BuildableClass : Buildable.registry.keySet())
                if (CategoryClass.isAssignableFrom(BuildableClass))
                    BuildableClasses.add(BuildableClass);
        return BuildableClasses;
    }

    class BottomPanel extends JPanel {
        BottomPanel() {
            super(new BorderLayout());
            this.setPreferredSize(new Dimension(0, 400));

            this.add(new TimeSpeedButtons(), BorderLayout.WEST);
        }

        class TimeSpeedButtons extends JPanel {
            TimeSpeedButtons() {
                super(new GridLayout(0, 1));
                // this.setPreferredSize(new Dimension(150, 0));
                this.add(new JLabel("Simulation Speed"));
                ArrayList<JButton> buttons = new ArrayList<>();
                for (GameSpeed speed : GameSpeed.values()) {
                    JButton button = new JButton(speed.toString());
                    if (simulator.gameSpeed == speed)
                        button.setEnabled(false);
                    this.add(button);
                    buttons.add(button);
                    button.addActionListener(e -> {
                        for (JButton b : buttons) {
                            b.setEnabled(true);
                        }
                        button.setEnabled(false);
                        simulator.gameSpeed = speed;
                    });
                }
            }
        }
    }
}
