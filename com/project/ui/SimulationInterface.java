package com.project.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.project.buildings.Buildable;
import com.project.buildings.Buildable.UnregisteredBuildingType;
import com.project.buildings.privatebuilding.PrivateBuilding;
import com.project.buildings.privatebuilding.residential.ResidentialBuilding;
import com.project.buildings.privatebuilding.workplace.WorkplaceBuilding;
import com.project.buildings.privatebuilding.workplace.commercial.CommercialBuilding;
import com.project.buildings.privatebuilding.workplace.industrial.IndustrialBuilding;
import com.project.buildings.privatebuilding.workplace.office.OfficeBuilding;
import com.project.buildings.publicbuilding.service.healthcare.HealthcareBuilding;
import com.project.buildings.publicbuilding.service.police.PoliceStation;
import com.project.buildings.publicbuilding.transportation.PublicTransportation;
import com.project.city.Citizen;
import com.project.city.City;
import com.project.loans.FederalLoan;
import com.project.loans.Loan;
import com.project.loans.PrivateLoan;
import com.project.simulation.GameSpeed;
import com.project.simulation.Simulator;
import com.project.ui.IsometricMapView.OverlayPainter;
import com.project.utils.Point;
import com.project.utils.Reactive.Observable;

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
                                }, building);
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
                return "Upgrade";
            };

            public String getStopName() {
                return "Stop upgrading";
            }

            public Runnable enable(Runnable onEnd) {
                return CityView.enableUpgradeAction(view, onEnd);
            }
        });
        actions.add(new TogglesMenu.Action() {
            public String getName() {
                return "Building Info Picker";
            };

            public String getStopName() {
                return "Close Building Info Picker";
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
        actions.add(new TogglesMenu.Action() {
            public String getName() {
                return "Citizen Info Picker";
            };

            public String getStopName() {
                return "Close Citizen Info Picker";
            }

            public Runnable enable(Runnable onEnd) {
                return CityView.enableBuildingHoverAction(view, (building) -> () -> {
                    String html = "<html>";

                    if (building.getVisitors().size() == 0)
                        html += "Nobody here";

                    int i = 0;
                    for (Citizen citizen : building.getVisitors()) {
                        if (i++ != 0)
                            html += "<hr/>";
                        for (Entry<String, String> entry : citizen.getDetailedInfo().entrySet()) {
                            html += "<b>" + entry.getKey() + ":</b> " + entry.getValue() + "<br/>";
                        }
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
            super();
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            this.setPreferredSize(new Dimension(0, 400));

            this.add(new TimeSpeedButtons());
            this.add(Box.createRigidArea(new Dimension(15, 0)));
            this.add(new Overlays());
            this.add(Box.createRigidArea(new Dimension(15, 0)));
            this.add(new Loans());
            this.add(Box.createRigidArea(new Dimension(15, 0)));
            this.add(new AggregateStats());
            this.add(Box.createRigidArea(new Dimension(15, 0)));
            this.add(new NumberStats());
            this.add(Box.createRigidArea(new Dimension(15, 0)));
            this.add(new DemandStats());
        }

        class Loans extends JPanel {
            Loans() {
                super(new BorderLayout());
                this.add(new ReactiveLabel<>(simulator.lastLoansServiceView,
                        service -> "Last service: " + service.intValue() + "$"), BorderLayout.NORTH);
                this.add(new FastScrollPane(new LoansList()), BorderLayout.CENTER);
                this.add(new LoansAdder(), BorderLayout.SOUTH);
            }

            static class FastScrollPane extends JScrollPane {
                FastScrollPane(Component component) {
                    super(component);
                    this.getVerticalScrollBar().setUnitIncrement(16);
                }
            }

            class LoansAdder extends JPanel {
                LoansAdder() {
                    super(new GridLayout(1, 0, 10, 10));
                    JButton federalLoan = new JButton("New Federal Loan");
                    federalLoan.addActionListener(e -> simulator.city.takeOutLoan(new FederalLoan()));
                    this.add(federalLoan);
                    JButton privateLoan = new JButton("New Private Loan");
                    privateLoan.addActionListener(e -> simulator.city.takeOutLoan(new PrivateLoan()));
                    this.add(privateLoan);
                }
            }

            class LoansList extends JPanel {
                LoansList() {
                    super(new GridLayout(0, 1));
                    simulator.city.loansView.subscribe(loans -> this.renderList(loans));
                }

                private void renderList(List<Loan> loans) {
                    this.removeAll();
                    for (Loan loan : loans) {
                        this.add(new LoanComponent(loan));
                    }
                    this.revalidate();
                    this.repaint();
                }

                class LoanComponent extends JPanel {
                    LoanComponent(Loan loan) {
                        super(new GridLayout(1, 0));
                        this.add(new JLabel(loan.getClass().getSimpleName()));
                        this.add(new ReactiveLabel<>(loan.paymentLeftView, left -> left + "$"));
                        JButton payout = new JButton("Payout");
                        simulator.city.moneyView
                                .subscribe(money -> payout.setEnabled(money > loan.paymentLeftView.get()));
                        payout.addActionListener(e -> simulator.city.payOutLoan(loan));
                        this.add(payout);
                    }
                }
            }
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
                                return 100 - p.getResidents().stream().map(r -> r.getCurrentHealth()).reduce(0,
                                        Integer::sum,
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
            private static int shortagePercentage(double val) {
                return (int) (val * 100 - 100);
            }

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
                this.add(new ReactiveLabel<>(simulator.currentTickView,
                        t -> "Housing shortage: "
                                + shortagePercentage(ResidentialBuilding.calculateHousingShortage(simulator.city))
                                + "%"));
                this.add(new ReactiveLabel<>(simulator.currentTickView,
                        t -> "Labor shortage: "
                                + shortagePercentage(WorkplaceBuilding.calculateLaborShortage(simulator.city))
                                + "%"));
                this.add(new ReactiveLabel<>(simulator.currentTickView,
                        t -> "Retail shortage: "
                                + shortagePercentage(CommercialBuilding.calculateRetailShortage(simulator.city))
                                + "%"));
            }
        }

        class AggregateStats extends JPanel {
            AggregateStats() {
                super(new GridLayout(0, 1));
                this.add(new ReactiveLabel<>(simulator.currentTickView,
                        t -> "Average health: " + simulator.averageCitizenHealth()));
                this.add(new ReactiveLabel<>(simulator.currentTickView,
                        t -> "Average crime rate: " + simulator.averageCrimeRate()));
                this.add(new ReactiveLabel<>(simulator.currentTickView,
                        t -> "Average satisfaction: " + simulator.averageCitizenSatisfaction()));
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
                super(new GridLayout(0, 1, 10, 10));
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
