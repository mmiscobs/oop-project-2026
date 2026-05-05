package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import buildings.Buildable;
import buildings.Buildable.UnregisteredBuildingType;
import buildings.privatebuilding.PrivateBuilding;
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
import ui.IsometricMapView.OverlayPainter;
import utils.Point;
import utils.Reactive.Observable;

public class SimulationInterface extends JPanel {
    SimulationInterface(Simulator simulator) {
        super(new BorderLayout());
        this.simulator = simulator;
        this.simulator.startSimulation();
        this.view = new CityView(simulator.city);

        this.simulator.currentTickView.subscribe((tick) -> {
            view.tick = tick;
            view.render();
        });

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
        JPanel left = new TogglesMenu(actions);
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
            JPanel stats = new JPanel(new GridLayout(1, 0));
            this.add(stats, BorderLayout.EAST);
            stats.add(new Overlays());
            stats.add(new NumberStats());
            stats.add(new DemandStats());
        }

        class Overlays extends JPanel {
            Overlays() {
                super(new GridLayout(0, 1));
                ArrayList<TogglesMenu.Action> actions = new ArrayList<>();
                Function<Function<Buildable, Integer>, OverlayPainter> createBuildingsOverlay = (stat) -> (g, v) -> {
                    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    Color stroke = new Color(255, 200, 40, 0);
                    for (Entry<Point, Buildable> entry : simulator.city.grid.buildings.entrySet()) {
                        Buildable buildable = entry.getValue();
                        Point point = entry.getKey();
                        Integer value = stat.apply(buildable);
                        if (value != null) {
                            Color fill = new Color(Math.clamp((int) (2.55 * value), 0, 255),
                                    Math.clamp((int) (255 - 2.55 * value), 0, 255),
                                    0, 110);
                            for (Point p : Point.allPointsWithin(point, buildable.getLength(),
                                    buildable.getWidth())) {
                                v.drawTileDiamond(g, p.x, p.y, fill, stroke);
                            }
                        }
                    }
                };
                actions.add(new TogglesMenu.Action() {
                    public String getName() {
                        return "Congestion overlay";
                    }

                    public String getStopName() {
                        return "Disable congestion overlay";
                    }

                    public Runnable enable(Runnable onEnd) {
                        return view.addOverlay(createBuildingsOverlay.apply(b -> {
                            if (b instanceof PublicTransportation p)
                                return p.getCongestion();
                            return null;
                        }));
                    }
                });
                actions.add(new TogglesMenu.Action() {
                    public String getName() {
                        return "Health overlay";
                    }

                    public String getStopName() {
                        return "Disable health overlay";
                    }

                    public Runnable enable(Runnable onEnd) {
                        return view.addOverlay(createBuildingsOverlay.apply(b -> {
                            if (b instanceof ResidentialBuilding p)
                                return p.getResidents().stream().map(r -> r.getCurrentHealth()).reduce(0, Integer::sum,
                                        Integer::sum) / p.getResidents().size();
                            return null;
                        }));
                    }
                });
                actions.add(new TogglesMenu.Action() {
                    public String getName() {
                        return "Profit overlay";
                    }

                    public String getStopName() {
                        return "Disable profit overlay";
                    }

                    public Runnable enable(Runnable onEnd) {
                        return view.addOverlay(createBuildingsOverlay.apply(b -> {
                            Optional<Integer> maxProfit = simulator.city.builtBuildings().stream()
                                    .map(bu -> bu.calculateProfitPerTick()).reduce(Integer::max);
                            if (b instanceof PrivateBuilding p)
                                return 50 - (int) Math.clamp(
                                        (double) p.calculateProfitPerTick() / maxProfit.orElse(1) * 50,
                                        0, 50);
                            return null;
                        }));
                    }
                });
                actions.add(new TogglesMenu.Action() {
                    public String getName() {
                        return "Crime overlay";
                    }

                    public String getStopName() {
                        return "Disable crime overlay";
                    }

                    public Runnable enable(Runnable onEnd) {
                        return view.addOverlay(createBuildingsOverlay.apply(b -> b.getCrimeRate()));
                    }
                });
                actions.add(new TogglesMenu.Action() {
                    public String getName() {
                        return "Noise overlay";
                    }

                    public String getStopName() {
                        return "Disable noise overlay";
                    }

                    public Runnable enable(Runnable onEnd) {
                        return view.addOverlay(createBuildingsOverlay.apply(b -> {
                            if (b instanceof PublicTransportation p)
                                return p.computeNoiseLevel();
                            return null;
                        }));
                    }
                });
                this.add(new TogglesMenu(actions));
            }
        }

        class DemandStats extends JPanel {
            DemandStats() {
                super(new GridLayout(0, 1));
                this.add(new ReactiveLabel<>(simulator.currentTickView, t -> "Current tick: " + t));
                this.add(new ReactiveLabel<>(simulator.currentTickView,
                        t -> "Residential demand: " + ResidentialBuilding.calculateDemand(simulator.city)));
                this.add(new ReactiveLabel<>(simulator.currentTickView,
                        t -> "Commercial demand: " + CommercialBuilding.calculateDemand(simulator.city)));
                this.add(new ReactiveLabel<>(simulator.currentTickView,
                        t -> "Office demand: " + OfficeBuilding.calculateDemand(simulator.city)));
                this.add(new ReactiveLabel<>(simulator.currentTickView,
                        t -> "Industrial demand: " + IndustrialBuilding.calculateDemand(simulator.city)));
            }
        }

        class NumberStats extends JPanel {
            NumberStats() {
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

        }

        class ReactiveLabel<T> extends JLabel {
            ReactiveLabel(Observable<T> observable, Function<T, String> labelFunction) {
                this.setText(labelFunction.apply(observable.get()));
                observable.subscribe(v -> this.setText(labelFunction.apply(v)));
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
