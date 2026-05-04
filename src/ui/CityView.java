package ui;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import buildings.Buildable;
import buildings.publicbuilding.transportation.PublicTransportation;
import buildings.publicbuilding.transportation.Road;
import buildings.publicbuilding.transportation.Street;
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

public class CityView extends IsometricMapView {
    private static final int TILE_W = 34;
    private static final int TILE_H = 17;
    private static final int COLS = 10;
    private static final int ROWS = 10;
    private City city;

    public CityView(City city) {
        super();
        this.city = city;

        buildView();
        JLabel info = makeInfoLabel();
        info.setVisible(false);
        this.attachComponent(info, 0, 0, IsometricMapView.TileAnchor.ABOVE, 0, -8);
        enableManhattanDragAction(this, info, tiles -> {
            for (Point point : tiles) {
                city.grid.placeBuildingAt(point, new Street());
            }
            this.render();
        });
        this.addTileClickListener((c, r, e) -> System.out.println("click " + c + "," + r));
    }

    public void render() {
        Image[][] grid = new Image[ROWS][COLS];

        try {
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
                                && buildingNorth.getClass() == building.getClass();
                        boolean hasSouthAdjacent = buildingSouth != null
                                && buildingSouth.getClass() == building.getClass();
                        boolean hasEastAdjacent = buildingEast != null
                                && buildingEast.getClass() == building.getClass();
                        boolean hasWestAdjacent = buildingWest != null
                                && buildingWest.getClass() == building.getClass();
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

    private static class DragVisual {
        boolean active;
        int sc, sr, cc, cr;
    }

    interface TilesListener {
        void actOnTiles(List<Point> tiles);
    }

    private static Runnable enableManhattanDragAction(CityView view, JLabel info, TilesListener onRoute) {
        DragVisual drag = new DragVisual();

        TileHoverListener onHover = (c, r) -> {
            if (c < 0) {
                info.setVisible(false);
            } else {
                info.setVisible(true);
                info.setText(infoHtml(c, r, drag));
                view.moveAttachment(info, c, r);
            }
        };
        view.addTileHoverListener(onHover);

        TileDragListener onDrag = new IsometricMapView.TileDragListener() {
            @Override
            public void onDragStart(int c, int r) {
                drag.active = true;
                drag.sc = c;
                drag.sr = r;
                drag.cc = c;
                drag.cr = r;
                view.repaint();
            }

            @Override
            public void onDragMove(int sc, int sr, int cc, int cr) {
                drag.cc = cc;
                drag.cr = cr;
                if (info.isVisible())
                    info.setText(infoHtml(cc, cr, drag));
                view.repaint();
            }

            @Override
            public void onDragEnd(int sc, int sr, int ec, int er) {
                onRoute.actOnTiles(manhattanPath(sc, sr, ec, er));
                drag.active = false;
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
            for (Point t : manhattanPath(drag.sc, drag.sr, drag.cc, drag.cr)) {
                v.drawTileDiamond(g, t.x, t.y, fill, stroke);
            }
            v.drawTileDiamond(g, drag.sc, drag.sr, null, new Color(60, 200, 255, 240));
            v.drawTileDiamond(g, drag.cc, drag.cr, null, new Color(255, 80, 80, 240));
        };
        view.setOverlay(routeOverlay);
        return () -> {
            view.removeTileHoverListener(onHover);
            view.removeTileDragListener(onDrag);
            view.removeOverlay(routeOverlay);
        };
    }

    private static List<Point> manhattanPath(int sc, int sr, int ec, int er) {
        List<Point> path = new ArrayList<>();
        int c = sc, r = sr;
        int dc = Integer.signum(ec - sc);
        int dr = Integer.signum(er - sr);
        path.add(new Point(c, r));
        while (c != ec) {
            c += dc;
            path.add(new Point(c, r));
        }
        while (r != er) {
            r += dr;
            path.add(new Point(c, r));
        }
        return path;
    }

    private void buildView() {
        super.setTileSize(TILE_W, TILE_H);
        super.setBackground(new Color(18, 22, 30));
        super.setTileSurfaceOffset(4);

        super.setZoom(5);
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

    private static String infoHtml(int c, int r, DragVisual drag) {
        if (drag != null && drag.active) {
            int len = manhattanPath(drag.sc, drag.sr, drag.cc, drag.cr).size();
            return "<html><b>Road preview</b><br/>" + len + " tiles &rarr; (" + c + ", " + r + ")</html>";
        }
        boolean isBuilding = (c == 4 && r == 3) || (c == 6 && r == 5) || (c == 2 && r == 6)
                || (c == 7 && r == 2) || (c == 7 && r == 7) || (c == 2 && r == 4);
        String label = isBuilding ? "Building" : "Ground";
        return "<html><b>" + label + "</b><br/>tile (" + c + ", " + r + ")</html>";
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
