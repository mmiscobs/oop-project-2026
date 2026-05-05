package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

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

public class SimulationInterface extends JPanel {
    SimulationInterface(Simulator simulator) {
        super(new BorderLayout());
        this.simulator = simulator;
        this.simulator.startSimulation();
        this.view = new CityView(simulator.city);

        this.simulator.onTick = (tick) -> {
            view.tick = tick;
            view.render();
        };

        view.render();
        final int ZOOM = 4;
        // view.setOrigin(TILE_W * COLS / 2 * ZOOM, 2 * TILE_H * ZOOM);
        view.setZoom(ZOOM);

        JPanel left = getBuildActionsPanel(simulator.city);
        JPanel bottom = new BottomPanel();

        this.add(left, BorderLayout.WEST);
        this.add(view, BorderLayout.CENTER);
        this.add(bottom, BorderLayout.SOUTH);
    }

    private CityView view;
    private Simulator simulator;

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
                                            city.build(Buildable.createBuilding(BuildableClass), point);
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
                                    city.build(building, tile);
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
                    city.demolish(loc);

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
            this.add(new Stats(), BorderLayout.EAST);
        }

        class Stats extends JPanel {
            Stats() {
                super(new GridLayout(0, 1));
                this.add(new MoneyLabel());
            }

            class MoneyLabel extends JLabel {
                MoneyLabel() {
                    this.update();
                    simulator.city.moneyView.subscribe(v -> this.update());
                }

                private void update() {
                    this.setText("Money: " + simulator.city.moneyView.get().intValue() + "$");
                }
            }
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
