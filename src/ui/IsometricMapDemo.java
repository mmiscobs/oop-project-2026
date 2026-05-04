package ui;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import buildings.Buildable;
import buildings.Buildable.UnregisteredBuildingType;
import buildings.publicbuilding.transportation.PublicTransportation;
import city.City;
import utils.Point;

public class IsometricMapDemo {
    private static final int TILE_W = 34;
    private static final int TILE_H = 16;
    private static final int COLS = 15;
    private static final int ROWS = 15;
    private static final int FRAME_W = 2000;
    private static final int FRAME_H = 900;

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(IsometricMapDemo::showWindow);
    }

    private static void showWindow() {
        City city = new City(COLS, ROWS);

        CityView view = new CityView(city, COLS, ROWS);

        view.render();
        final int ZOOM = 4;
        view.setOrigin(TILE_W * COLS / 2 * ZOOM, 2 * TILE_H * ZOOM);
        view.setZoom(ZOOM);

        JFrame f = new JFrame("City Map Demo");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        ArrayList<TogglesMenu.Action> actions = new ArrayList<>();

        for (Class<? extends Buildable> BuildableClass : Buildable.registry.keySet()) {
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
        JPanel top = new TogglesMenu("Build", actions);

        root.add(top, BorderLayout.WEST);
        root.add(view, BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();
        f.setJMenuBar(menuBar);
        f.setContentPane(root);
        f.setSize(FRAME_W, FRAME_H);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}
