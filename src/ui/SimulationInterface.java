package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

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
import utils.Reactive.Observable;

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
                    try {
                        if (PublicTransportation.class.isAssignableFrom(BuildableClass)) {
                            Buildable example = Buildable.createBuilding(BuildableClass);
                            return CityView.enableManhattanDragAction(view,
                                    (dragInfo) -> () -> {
                                        int totalPrice = 0;

                                        for (Point point : CityView.manhattanPath(dragInfo.from, dragInfo.to)) {
                                            Buildable existingBuilding = city.grid.getBuildingAt(point);
                                            if (existingBuilding == null)
                                                totalPrice += example.getPrice();
                                        }
                                        return "<html><b>" + getPrettyName() + " preview</b><br/>"
                                                + CityView.manhattanPath(dragInfo.from, dragInfo.to).size()
                                                + " tiles &rarr; ("
                                                + dragInfo.to.x + ", " + dragInfo.to.y + ")<br/>"
                                                + totalPrice
                                                + "$ total</html>";
                                    },
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
                        }
                        Buildable building = Buildable.createBuilding(BuildableClass);
                        return CityView.enableBuildAction(view,
                                (loc) -> () -> building.getClass().getSimpleName() + " (" + building.getPrice() + "$)",
                                building.getLength(),
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
        actions.add(new TogglesMenu.Action() {
            public String getName() {
                return "Info Picker";
            };

            public String getStopName() {
                return "Close Info Picker";
            }

            public Runnable enable(Runnable onEnd) {
                return CityView.enableBuildingHoverAction(view, (building) -> () -> {
                    String html = "<html>";

                    for (Entry<String, String> entry : building.getDetailedInfo().entrySet()) {
                        html += "<b>" + entry.getKey() + ":</b> " + entry.getValue() + "<br/>";
                    }
                    return html + "</html>";
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
                this.add(new ReactiveLabel<>(simulator.city.moneyView, a -> "Money: " + a.intValue()));
                this.add(new ReactiveLabel<>(simulator.citizensAmountView, a -> "Population: " + a));
                this.add(new ReactiveLabel<>(simulator.homelessCitizensAmountView, a -> "Homeless: " + a));
                this.add(new ReactiveLabel<>(simulator.lastBuildingsUpkeepView, a -> "Upkeep: -" + a + "$"));
                this.add(new ReactiveLabel<>(simulator.lastBusinessTaxView, a -> "Business Tax: " + a + "$"));
                this.add(new ReactiveLabel<>(simulator.lastPurchaseTaxView, a -> "Purchase Tax: " + a + "$"));
                this.add(new ReactiveLabel<>(simulator.lastResidentTaxView, a -> "Resident Tax: " + a + "$"));
                this.add(new ReactiveLabel<>(simulator.netIncomeView, a -> "Net Income: " + a + "$"));
            }

            class ReactiveLabel<T> extends JLabel {
                ReactiveLabel(Observable<T> observable, Function<T, String> labelFunction) {
                    this.setText(labelFunction.apply(observable.get()));
                    observable.subscribe(v -> this.setText(labelFunction.apply(v)));
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
