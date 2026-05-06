package com.project.ui;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.project.buildings.Buildable;
import com.project.buildings.privatebuilding.PrivateBuilding;
import com.project.buildings.publicbuilding.PublicBuilding;
import com.project.buildings.publicbuilding.PublicBuilding.Upgrade;
import com.project.buildings.publicbuilding.service.PublicServiceBuilding;
import com.project.buildings.publicbuilding.transportation.PublicTransportation;
import com.project.city.City;
import com.project.utils.Point;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

public class CityView extends IsometricMapView {
    private static final int TILE_W = 34;
    private static final int TILE_H = 17;
    private final int COLS;
    private final int ROWS;
    private City city;

    public CityView(City city) {
        super();
        this.COLS = city.grid.sizeX;
        this.ROWS = city.grid.sizeY;
        this.city = city;

        buildView();
    }

    public int tick;

    private ArrayList<Runnable> renderListeners = new ArrayList<>();

    public void render() {
        Image[][] grid = new Image[ROWS][COLS];

        this.clearSprites();

        try {
            Image cloud = loadImage(getClass(), "./clouds.png");
            Random rng = new Random(42);
            for (int i = -ROWS; i < ROWS * 2; i++) {
                this.addSprite(
                        new Sprite(cloud, (tick + rng.nextInt(0, COLS * 3)) % (COLS * 3) - COLS, i, 1, 1, true));
            }
            Image grass1 = loadImage(getClass(), "./grass.png");

            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    grid[r][c] = grass1;
                }
            }
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    Buildable building = city.grid.buildings.get(new Point(r, c));
                    if (building == null)
                        continue;
                    String suffix = "";
                    String traffic = null;
                    if (building instanceof PublicTransportation p) {
                        Buildable buildingNorth = city.grid.buildings.get(new Point(r, c + 1));
                        Buildable buildingSouth = city.grid.buildings.get(new Point(r, c - 1));
                        Buildable buildingEast = city.grid.buildings.get(new Point(r + 1, c));
                        Buildable buildingWest = city.grid.buildings.get(new Point(r - 1, c));
                        boolean hasNorthAdjacent = buildingNorth != null
                                && buildingNorth instanceof PublicTransportation;
                        boolean hasSouthAdjacent = buildingSouth != null
                                && buildingSouth instanceof PublicTransportation;
                        boolean hasEastAdjacent = buildingEast != null
                                && buildingEast instanceof PublicTransportation;
                        boolean hasWestAdjacent = buildingWest != null
                                && buildingWest instanceof PublicTransportation;
                        boolean isNorthSouth = hasNorthAdjacent || hasSouthAdjacent;
                        boolean isWestEast = hasEastAdjacent || hasWestAdjacent;
                        suffix = "Intersection";
                        if (isNorthSouth && !isWestEast) {
                            suffix = "NorthSouth";
                        } else if (isWestEast && !isNorthSouth) {
                            suffix = "WestEast";
                        }
                        if (!suffix.equals("Intersection"))
                            if (p.getCongestion() > 60) {
                                traffic = "HighTraffic";
                            } else if (p.getCongestion() > 15) {
                                traffic = "LowTraffic";
                            }
                    }
                    if (building instanceof PrivateBuilding privateBuilding && !privateBuilding.getIsBuilt()) {
                        for (Point tileWithin : Point.allPointsWithin(
                                new Point(c - building.getWidth() + 1, r - building.getLength() + 1),
                                building.getLength(),
                                building.getWidth())) {
                            this.addSprite(new Sprite(
                                    loadImage(getClass(),
                                            "./construction.png"),
                                    tileWithin.y,
                                    tileWithin.x,
                                    building.getWidth(), building.getLength()));
                        }
                    } else {
                        this.addSprite(new Sprite(
                                loadImage(building.getClass(),
                                        "./" + building.getClass().getSimpleName() + suffix + ".png"),
                                r,
                                c,
                                building.getWidth(), building.getLength()));
                        if (traffic != null)
                            this.addSprite(new Sprite(
                                    loadImage(building.getClass(),
                                            "./" + traffic + suffix + ".png"),
                                    r,
                                    c,
                                    1, 1));
                    }
                }
            }
        } catch (IOException e) {
            System.err.print(e);
        }

        for (Runnable listener : renderListeners) {
            listener.run();
        }
        super.setTiles(grid);
    }

    public static class DragVisual {
        boolean active;
        Point from;
        Point to;
    }

    interface TilesListener {
        void actOnTiles(List<Point> tiles, Runnable cleanup);
    }

    interface TileListener {
        void actOnTile(Point tile, Runnable cleanup);
    }

    interface FloatingDragLabelCreator {
        Supplier<String> produceLabelHTML(DragVisual dragInfo);
    }

    interface FloatingHoverLabelCreator {
        Supplier<String> produceLabelHTML(Point loc);
    }

    interface FloatingBuildingHoverLabelCreator {
        Supplier<String> produceLabelHTML(Buildable building);
    }

    public static Runnable enableManhattanDragAction(CityView view, FloatingDragLabelCreator labelCreator,
            TilesListener onRoute) {
        ReactiveInfoLabel info = view.new ReactiveInfoLabel(null);
        view.attachComponent(info, 0, 0, IsometricMapView.TileAnchor.ABOVE, 0, -8);
        DragVisual drag = new DragVisual();

        TileHoverListener onHover = (loc) -> {
            view.moveAttachment(info, loc.x, loc.y);
        };
        view.addTileHoverListener(onHover);

        Runnable[] cleanup = new Runnable[1];
        TileDragListener onDrag = new IsometricMapView.TileDragListener() {
            @Override
            public void onDragStart(Point loc) {
                drag.active = true;
                drag.from = loc;
                drag.to = loc;
                info.setLabelCreator(labelCreator.produceLabelHTML(drag));
                view.repaint();
            }

            @Override
            public void onDragMove(Point from, Point to) {
                drag.to = to;
                info.setLabelCreator(labelCreator.produceLabelHTML(drag));
                view.repaint();
            }

            @Override
            public void onDragEnd(Point from, Point to) {
                onRoute.actOnTiles(manhattanPath(from, to), cleanup[0]);
                drag.active = false;
                info.setLabelCreator(null);
                info.setVisible(false);
                view.repaint();
            }
        };
        view.addTileDragListener(onDrag);

        OverlayPainter routeOverlay = (g, v) -> {
            if (!drag.active)
                return;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = new Color(255, 220, 90, 110);
            Color stroke = new Color(255, 200, 40, 230);
            for (Point t : manhattanPath(drag.from, drag.to)) {
                v.drawTileDiamond(g, t.x, t.y, fill, stroke);
            }
            v.drawTileDiamond(g, drag.from.x, drag.from.y, null, new Color(60, 200, 255, 240));
            v.drawTileDiamond(g, drag.to.x, drag.from.y, null, new Color(255, 80, 80, 240));
        };
        Runnable removeOverlay = view.addOverlay(routeOverlay);
        cleanup[0] = () -> {
            SwingUtilities.invokeLater(() -> {
                view.detachComponent(info);
                view.removeTileDragListener(onDrag);
                view.removeTileHoverListener(onHover);
                removeOverlay.run();
            });
        };
        return cleanup[0];
    }

    public static Runnable enableBuildAction(CityView view, FloatingHoverLabelCreator labelCreator, int l, int w,
            TileListener onTile, Buildable buildable) {
        ReactiveInfoLabel info = view.new ReactiveInfoLabel(null);
        view.attachComponent(info, 0, 0, IsometricMapView.TileAnchor.ABOVE, 0, -8);
        class HoverLoc {
            Point loc;

            boolean isWithinGrid() {
                return !(loc == null || loc.x < 0 || loc.x + w > view.COLS
                        || loc.y + l > view.ROWS);
            }
        }
        HoverLoc currentHoverLoc = new HoverLoc();

        TileHoverListener onHover = (loc) -> {
            currentHoverLoc.loc = loc;
            view.moveAttachment(info, loc.x, loc.y);
            info.setLabelCreator(currentHoverLoc.isWithinGrid() ? labelCreator.produceLabelHTML(loc) : null);
        };
        view.addTileHoverListener(onHover);

        Runnable[] cleanup = new Runnable[1];

        TileClickListener onClick = (loc, ev) -> {
            if (!currentHoverLoc.isWithinGrid())
                return;
            onTile.actOnTile(loc, cleanup[0]);
            cleanup[0].run();
        };
        view.addTileClickListener(onClick);

        OverlayPainter routeOverlay = (g, v) -> {
            if (!currentHoverLoc.isWithinGrid())
                return;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = new Color(255, 220, 90, 110);
            Color stroke = new Color(255, 200, 40, 230);
            Color prohibitedFill = new Color(178, 34, 34, 110);
            for (Point t : Point.allPointsWithin(currentHoverLoc.loc, l, w)) {
                Buildable existingBuilding = view.city.grid.getBuildingAt(t);
                v.drawTileDiamond(g, t.x, t.y, existingBuilding != null ? prohibitedFill : fill, stroke);
            }
            if (buildable instanceof PublicServiceBuilding s) {
                Map<Point, PublicServiceBuilding> publicServiceTypeBuildings = PublicServiceBuilding
                        .getAllPublicServiceTypeBuildings(view.city,
                                s.getPublicServiceTypeClass());

                publicServiceTypeBuildings.put(currentHoverLoc.loc, s);
                Function<Point, Double> combinedField = PublicServiceBuilding
                        .getCombinedFieldFunctionForBuildings(publicServiceTypeBuildings);
                for (Point t : Point.allPointsWithin(new Point(0, 0), view.city.grid.sizeX, view.city.grid.sizeY)) {
                    int colorVal = combinedField.apply(t).intValue();

                    Color valueFill = new Color(255 - colorVal, colorVal, 0,
                            110);
                    v.drawTileDiamond(g, t.x, t.y, valueFill, stroke);
                }
            }
        };
        Runnable removeOverlay = view.addOverlay(routeOverlay);
        cleanup[0] = () -> {
            SwingUtilities.invokeLater(() -> {
                view.detachComponent(info);
                view.removeTileClickListener(onClick);
                view.removeTileHoverListener(onHover);
                removeOverlay.run();
            });
        };
        return cleanup[0];
    }

    public static Runnable enableBuildingHoverAction(CityView view,
            FloatingBuildingHoverLabelCreator onBuilding) {
        ReactiveInfoLabel info = view.new ReactiveInfoLabel(null);
        view.attachComponent(info, 0, 0, IsometricMapView.TileAnchor.ABOVE, 0, -8);
        class HoverLoc {
            Point loc;

            boolean isWithinGrid() {
                return !(loc == null || loc.x < 0 || loc.x > view.COLS
                        || loc.y > view.ROWS);
            }
        }
        HoverLoc currentHoverLoc = new HoverLoc();

        FloatingHoverLabelCreator labelCreator = (loc) -> {
            Buildable existingBuilding = view.city.grid.getBuildingAt(loc);
            if (existingBuilding != null) {
                return onBuilding.produceLabelHTML(existingBuilding);
            }
            return null;
        };

        TileHoverListener onHover = (loc) -> {
            currentHoverLoc.loc = loc;
            view.moveAttachment(info, loc.x, loc.y);
            info.setLabelCreator(currentHoverLoc.isWithinGrid() ? labelCreator.produceLabelHTML(loc) : null);
        };
        view.addTileHoverListener(onHover);

        Runnable[] cleanup = new Runnable[1];

        OverlayPainter routeOverlay = (g, v) -> {
            if (!currentHoverLoc.isWithinGrid())
                return;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = new Color(255, 220, 90, 110);
            Color stroke = new Color(255, 200, 40, 230);
            Buildable existingBuilding = view.city.grid.getBuildingAt(currentHoverLoc.loc);
            if (existingBuilding != null)
                for (Point t : Point.allPointsWithin(view.city.grid.getBuildingOrigin(existingBuilding),
                        existingBuilding.getLength(),
                        existingBuilding.getWidth())) {
                    v.drawTileDiamond(g, t.x, t.y, fill, stroke);
                }
            else
                v.drawTileDiamond(g, currentHoverLoc.loc.x, currentHoverLoc.loc.y, fill, stroke);
        };
        Runnable removeOverlay = view.addOverlay(routeOverlay);
        cleanup[0] = () -> {
            SwingUtilities.invokeLater(() -> {
                view.detachComponent(info);
                view.removeTileHoverListener(onHover);
                removeOverlay.run();
            });
        };
        return cleanup[0];
    }

    public static Runnable enableDemolishAction(CityView view,

            TileListener onTile) {
        ReactiveInfoLabel info = view.new ReactiveInfoLabel(null);
        view.attachComponent(info, 0, 0, IsometricMapView.TileAnchor.ABOVE, 0, -8);
        class HoverLoc {
            Point loc;

            boolean isWithinGrid() {
                return !(loc == null || loc.x < 0 || loc.x > view.COLS
                        || loc.y > view.ROWS);
            }
        }
        HoverLoc currentHoverLoc = new HoverLoc();

        FloatingHoverLabelCreator labelCreator = (loc) -> {
            Buildable existingBuilding = view.city.grid.getBuildingAt(loc);
            if (existingBuilding != null) {
                return () -> "Demolish " + existingBuilding.getClass().getSimpleName() + "("
                        + existingBuilding.getPrice() * City.DEMOLISHMENT_COEF + "$)";
            }
            return null;
        };

        TileHoverListener onHover = (loc) -> {
            currentHoverLoc.loc = loc;
            view.moveAttachment(info, loc.x, loc.y);
            info.setLabelCreator(currentHoverLoc.isWithinGrid() ? labelCreator.produceLabelHTML(loc) : null);
        };
        view.addTileHoverListener(onHover);

        Runnable[] cleanup = new Runnable[1];

        TileClickListener onClick = (loc, ev) -> {
            if (!currentHoverLoc.isWithinGrid())
                return;
            onTile.actOnTile(loc, cleanup[0]);
            cleanup[0].run();
        };
        view.addTileClickListener(onClick);

        OverlayPainter routeOverlay = (g, v) -> {
            if (!currentHoverLoc.isWithinGrid())
                return;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = new Color(255, 220, 90, 110);
            Color demolishFill = new Color(178, 34, 34, 110);
            Color stroke = new Color(255, 200, 40, 230);
            Buildable existingBuilding = view.city.grid.getBuildingAt(currentHoverLoc.loc);
            if (existingBuilding != null)
                for (Point t : Point.allPointsWithin(view.city.grid.getBuildingOrigin(existingBuilding),
                        existingBuilding.getLength(),
                        existingBuilding.getWidth())) {
                    v.drawTileDiamond(g, t.x, t.y, demolishFill, stroke);
                }
            else
                v.drawTileDiamond(g, currentHoverLoc.loc.x, currentHoverLoc.loc.y, fill, stroke);
        };
        Runnable removeOverlay = view.addOverlay(routeOverlay);
        cleanup[0] = () -> {
            SwingUtilities.invokeLater(() -> {
                view.detachComponent(info);
                view.removeTileClickListener(onClick);
                view.removeTileHoverListener(onHover);
                removeOverlay.run();
            });
        };
        return cleanup[0];
    }

    public static Runnable enableUpgradeAction(CityView view, Runnable onClose) {
        ReactiveInfoLabel info = view.new ReactiveInfoLabel(null);
        view.attachComponent(info, 0, 0, IsometricMapView.TileAnchor.ABOVE, 0, -8);
        class HoverLoc {
            Point loc;

            boolean isWithinGrid() {
                return !(loc == null || loc.x < 0 || loc.x > view.COLS
                        || loc.y > view.ROWS);
            }
        }
        HoverLoc currentHoverLoc = new HoverLoc();

        FloatingHoverLabelCreator labelCreator = (loc) -> {
            Buildable existingBuilding = view.city.grid.getBuildingAt(loc);
            if (existingBuilding != null) {
                if (existingBuilding instanceof PublicBuilding publicBuilding)
                    return () -> {
                        String html = "<html>";

                        if (publicBuilding.getUpgrades().length == 0)
                            html += "No upgrades";
                        for (Upgrade upgrade : publicBuilding.getUpgrades()) {
                            html += "<b>" + upgrade.getName() + "</b>: "
                                    + (upgrade.getIsBuilt() ? "built" : "not built") + " (" + upgrade.getPrice()
                                    + "$)<br/>";
                        }

                        return html + "</html>";
                    };
                return () -> "<html>No upgrades for private buildings</html>";
            }
            return null;

        };

        TileHoverListener onHover = (loc) -> {
            currentHoverLoc.loc = loc;
            view.moveAttachment(info, loc.x, loc.y);
            info.setLabelCreator(currentHoverLoc.isWithinGrid() ? labelCreator.produceLabelHTML(loc) : null);
        };
        view.addTileHoverListener(onHover);

        Runnable[] cleanup = new Runnable[1];

        TileClickListener onClick = (loc, ev) -> {
            if (!currentHoverLoc.isWithinGrid())
                return;
            Buildable buildable = view.city.grid.getBuildingAt(loc);
            if (buildable instanceof PublicBuilding publicBuilding && publicBuilding.getUpgrades().length > 0) {
                JPanel panel = new JPanel(new GridLayout(0, 1));
                Function<Upgrade, String> upgradeLabel = upgrade -> upgrade.getName() + " (" + (!upgrade.getIsBuilt()
                        ? (upgrade.getPrice()
                                + "$")
                        : "Built") + ")";
                for (Upgrade upgrade : publicBuilding.getUpgrades()) {
                    JButton upgradeButton = new JButton(upgradeLabel.apply(upgrade));
                    upgradeButton.setEnabled(!upgrade.getIsBuilt());

                    upgradeButton.addActionListener(e -> {
                        upgrade.build(view.city);
                        if (upgrade.getIsBuilt()) {
                            upgradeButton.setEnabled(false);
                            upgradeButton.setText(upgradeLabel.apply(upgrade));
                        }
                    });
                    panel.add(upgradeButton);
                }
                JButton closeButton = new JButton("Close");
                closeButton.addActionListener(e -> {
                    view.detachComponent(panel);
                    onClose.run();
                    cleanup[0].run();
                });
                panel.add(closeButton);
                view.attachComponent(panel, currentHoverLoc.loc.x, currentHoverLoc.loc.y,
                        IsometricMapView.TileAnchor.ABOVE);

            }
            // cleanup[0].run();
        };
        view.addTileClickListener(onClick);

        OverlayPainter routeOverlay = (g, v) -> {
            if (!currentHoverLoc.isWithinGrid())
                return;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = new Color(255, 220, 90, 110);
            Color demolishFill = new Color(178, 34, 34, 110);
            Color stroke = new Color(255, 200, 40, 230);
            Buildable existingBuilding = view.city.grid.getBuildingAt(currentHoverLoc.loc);
            if (existingBuilding != null)
                for (Point t : Point.allPointsWithin(view.city.grid.getBuildingOrigin(existingBuilding),
                        existingBuilding.getLength(),
                        existingBuilding.getWidth())) {
                    v.drawTileDiamond(g, t.x, t.y, demolishFill, stroke);
                }
            else
                v.drawTileDiamond(g, currentHoverLoc.loc.x, currentHoverLoc.loc.y, fill, stroke);
        };
        Runnable removeOverlay = view.addOverlay(routeOverlay);
        cleanup[0] = () -> {
            SwingUtilities.invokeLater(() -> {
                view.detachComponent(info);
                view.removeTileClickListener(onClick);
                view.removeTileHoverListener(onHover);
                removeOverlay.run();
            });
        };
        return cleanup[0];
    }

    public static List<Point> manhattanPath(Point from, Point to) {
        List<Point> path = new ArrayList<>();
        int c = from.x, r = from.y;
        int dc = Integer.signum(to.x - from.x);
        int dr = Integer.signum(to.y - from.y);
        path.add(new Point(c, r));
        while (c != to.x) {
            c += dc;
            path.add(new Point(c, r));
        }
        while (r != to.y) {
            r += dr;
            path.add(new Point(c, r));
        }
        return path;
    }

    private void buildView() {
        super.setTileSize(TILE_W, TILE_H);
        super.setBackground(new Color(18, 22, 30));
        super.setTileSurfaceOffset(4);

        super.setZoom(1);
    }

    private class ReactiveInfoLabel extends JLabel {
        private Supplier<String> labelCreator;

        private Runnable render;

        public void setLabelCreator(Supplier<String> labelCreator) {
            this.labelCreator = labelCreator;
            this.render.run();
        }

        ReactiveInfoLabel(Supplier<String> labelCreator) {
            this.labelCreator = labelCreator;
            this.setOpaque(true);
            this.setFont(new Font("SansSerif", Font.PLAIN, 12));
            this.setBackground(new Color(255, 252, 230));
            this.setForeground(new Color(30, 30, 30));
            this.setBorder(new CompoundBorder(
                    new LineBorder(new Color(40, 40, 40)),
                    new EmptyBorder(4, 8, 4, 8)));
            this.render = () -> {
                if (this.labelCreator != null) {
                    String res = this.labelCreator.get();
                    if (res != null)
                        this.setText(res);
                    this.setVisible(res != null);
                } else {
                    this.setVisible(false);
                }
            };
            render.run();
            renderListeners.add(render);
        }
    }

    public static BufferedImage loadImage(Class<?> Class, String filename) throws IOException {
        try (InputStream is = Class.getResourceAsStream(filename)) {
            if (is == null) {
                throw new IOException("Resource not found: " + filename);
            }
            return ImageIO.read(is);
        }
    }
}
