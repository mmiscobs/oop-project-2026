package ui;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import buildings.Buildable;
import buildings.publicbuilding.transportation.PublicTransportation;
import city.City;
import utils.Point;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CityView extends IsometricMapView {
    private static final int TILE_W = 34;
    private static final int TILE_H = 17;
    private final int COLS;
    private final int ROWS;
    private City city;

    public CityView(City city, int cols, int rows) {
        super();
        this.COLS = cols;
        this.ROWS = rows;
        this.city = city;

        buildView();
    }

    public int tick;

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
                    if (building instanceof PublicTransportation) {
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
                    }
                    this.addSprite(new Sprite(
                            loadImage(building.getClass(),
                                    "./" + building.getClass().getSimpleName() + suffix + ".png"),
                            r,
                            c,
                            building.getWidth(), building.getLength()));
                }
            }
        } catch (IOException e) {
            System.err.print(e);
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
        String produceLabelHTML(DragVisual dragInfo);
    }

    interface FloatingHoverLabelCreator {
        String produceLabelHTML(Point loc);
    }

    public static Runnable enableManhattanDragAction(CityView view, FloatingDragLabelCreator labelCreator,
            TilesListener onRoute) {
        JLabel info = makeInfoLabel();
        info.setVisible(false);
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
                info.setVisible(true);
                drag.active = true;
                drag.from = loc;
                drag.to = loc;
                view.repaint();
            }

            @Override
            public void onDragMove(Point from, Point to) {
                drag.to = to;
                if (info.isVisible())
                    info.setText(labelCreator.produceLabelHTML(drag));
                view.repaint();
            }

            @Override
            public void onDragEnd(Point from, Point to) {
                onRoute.actOnTiles(manhattanPath(from, to), cleanup[0]);
                drag.active = false;
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
        view.setOverlay(routeOverlay);
        cleanup[0] = () -> {
            SwingUtilities.invokeLater(() -> {
                view.detachComponent(info);
                view.removeTileDragListener(onDrag);
                view.removeTileHoverListener(onHover);
                view.setOverlay(null);
            });
        };
        return cleanup[0];
    }

    public static Runnable enableBuildAction(CityView view, FloatingHoverLabelCreator labelCreator, int l, int w,
            TileListener onTile) {
        JLabel info = makeInfoLabel();
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
            info.setVisible(currentHoverLoc.isWithinGrid());
            view.moveAttachment(info, loc.x, loc.y);
            info.setText(labelCreator.produceLabelHTML(loc));
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
        };
        view.setOverlay(routeOverlay);
        cleanup[0] = () -> {
            SwingUtilities.invokeLater(() -> {
                view.detachComponent(info);
                view.removeTileClickListener(onClick);
                view.removeTileHoverListener(onHover);
                view.setOverlay(null);
            });
        };
        return cleanup[0];
    }

    public static Runnable enableDemolishAction(CityView view,
            TileListener onTile) {
        JLabel info = makeInfoLabel();
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
                return "Demolish " + existingBuilding.getClass().getSimpleName();
            }
            return null;
        };

        TileHoverListener onHover = (loc) -> {
            currentHoverLoc.loc = loc;
            view.moveAttachment(info, loc.x, loc.y);
            String label = labelCreator.produceLabelHTML(loc);
            if (label != null && currentHoverLoc.isWithinGrid()) {
                info.setVisible(true);
                info.setText(label);
            } else
                info.setVisible(false);
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
        view.setOverlay(routeOverlay);
        cleanup[0] = () -> {
            SwingUtilities.invokeLater(() -> {
                view.detachComponent(info);
                view.removeTileClickListener(onClick);
                view.removeTileHoverListener(onHover);
                view.setOverlay(null);
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

    private static JLabel makeInfoLabel() {
        JLabel info = new JLabel();
        info.setOpaque(true);
        info.setFont(new Font("SansSerif", Font.PLAIN, 12));
        info.setBackground(new Color(255, 252, 230));
        info.setForeground(new Color(30, 30, 30));
        info.setBorder(new CompoundBorder(
                new LineBorder(new Color(40, 40, 40)),
                new EmptyBorder(4, 8, 4, 8)));
        return info;
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
