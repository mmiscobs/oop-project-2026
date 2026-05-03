package ui;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IsometricMapDemo {

    private static final int TILE_W = 64;
    private static final int TILE_H = 32;
    private static final int COLS = 10;
    private static final int ROWS = 10;
    private static final int FRAME_W = 800;
    private static final int FRAME_H = 520;

    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeLater(IsometricMapDemo::showWindow);
    }

    private static void showWindow() {
        IsometricMapView view = buildView();
        JLabel info = makeInfoLabel();
        info.setVisible(false);
        view.attachComponent(info, 0, 0, IsometricMapView.TileAnchor.ABOVE, 0, -8);
        installListeners(view, info);

        JFrame f = new JFrame("Isometric Map Demo");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setContentPane(view);
        f.setSize(FRAME_W, FRAME_H);
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }

    private static class DragVisual {
        boolean active;
        int sc, sr, cc, cr;
    }

    private static void installListeners(IsometricMapView view, JLabel info) {
        DragVisual drag = new DragVisual();

        view.addTileHoverListener((c, r) -> {
            if (c < 0) {
                info.setVisible(false);
            } else {
                info.setVisible(true);
                info.setText(infoHtml(c, r, drag));
                view.moveAttachment(info, c, r);
            }
        });

        view.addTileClickListener((c, r, e) -> System.out.println("click " + c + "," + r));

        view.addTileDragListener(new IsometricMapView.TileDragListener() {
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
                System.out.println("drag " + sc + "," + sr + " -> " + ec + "," + er
                        + " (" + manhattanPath(sc, sr, ec, er).size() + " tiles)");
                drag.active = false;
                view.repaint();
            }
        });

        view.setOverlay((g, v) -> {
            if (!drag.active)
                return;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = new Color(255, 220, 90, 110);
            Color stroke = new Color(255, 200, 40, 230);
            for (int[] t : manhattanPath(drag.sc, drag.sr, drag.cc, drag.cr)) {
                v.drawTileDiamond(g, t[0], t[1], fill, stroke);
            }
            v.drawTileDiamond(g, drag.sc, drag.sr, null, new Color(60, 200, 255, 240));
            v.drawTileDiamond(g, drag.cc, drag.cr, null, new Color(255, 80, 80, 240));
        });
    }

    private static List<int[]> manhattanPath(int sc, int sr, int ec, int er) {
        List<int[]> path = new ArrayList<>();
        int c = sc, r = sr;
        int dc = Integer.signum(ec - sc);
        int dr = Integer.signum(er - sr);
        path.add(new int[] { c, r });
        while (c != ec) {
            c += dc;
            path.add(new int[] { c, r });
        }
        while (r != er) {
            r += dr;
            path.add(new int[] { c, r });
        }
        return path;
    }

    private static IsometricMapView buildView() {
        IsometricMapView view = new IsometricMapView();
        view.setTileSize(TILE_W, TILE_H);
        view.setBackground(new Color(18, 22, 30));

        Image grass1 = makeGroundTile(new Color(85, 130, 65));
        Image grass2 = makeGroundTile(new Color(95, 145, 70));
        Image sand = makeGroundTile(new Color(210, 195, 140));
        Image stone = makeGroundTile(new Color(140, 140, 150));
        Image water = makeGroundTile(new Color(70, 110, 170));

        Image[][] grid = new Image[ROWS][COLS];
        Random rng = new Random(7);
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                double dx = c - (COLS - 1) / 2.0;
                double dy = r - (ROWS - 1) / 2.0;
                double dist = Math.sqrt(dx * dx + dy * dy);
                Image t;
                if (dist > 5.4)
                    t = water;
                else if (dist > 4.5)
                    t = sand;
                else if (rng.nextInt(7) == 0)
                    t = stone;
                else
                    t = (rng.nextBoolean() ? grass1 : grass2);
                grid[r][c] = t;
            }
        }

        grid[3][4] = makeBuilding(60, new Color(180, 80, 70), new Color(120, 60, 50));
        grid[5][6] = makeBuilding(95, new Color(180, 175, 160), new Color(90, 70, 55));
        grid[6][2] = makeBuilding(45, new Color(220, 180, 100), new Color(110, 80, 50));
        grid[2][7] = makeBuilding(115, new Color(120, 130, 200), new Color(80, 90, 140));
        grid[7][7] = makeBuilding(70, new Color(220, 160, 200), new Color(120, 70, 110));
        grid[4][2] = makeBuilding(50, new Color(180, 220, 210), new Color(90, 130, 130));

        view.setTiles(grid);
        view.setOrigin(FRAME_W / 2, 70);
        return view;
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

    private static BufferedImage makeGroundTile(Color base) {
        BufferedImage img = new BufferedImage(TILE_W, TILE_H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int[] xs = { TILE_W / 2, TILE_W - 1, TILE_W / 2, 0 };
        int[] ys = { 0, TILE_H / 2, TILE_H - 1, TILE_H / 2 };
        g.setColor(base);
        g.fillPolygon(xs, ys, 4);
        g.setColor(shift(base, -25));
        g.setStroke(new BasicStroke(1f));
        g.drawPolygon(xs, ys, 4);

        Random rng = new Random(base.getRGB());
        int halfW = TILE_W / 2;
        int halfH = TILE_H / 2;
        for (int i = 0; i < 14; i++) {
            int rx = rng.nextInt(TILE_W);
            int ry = rng.nextInt(TILE_H);
            int dx = Math.abs(rx - halfW);
            int dy = Math.abs(ry - halfH);
            if (dx * halfH + dy * halfW > halfW * halfH - 4)
                continue;
            g.setColor(shift(base, rng.nextInt(40) - 20));
            g.fillRect(rx, ry, 2, 1);
        }
        g.dispose();
        return img;
    }

    private static BufferedImage makeBuilding(int height, Color body, Color roof) {
        int H = height;
        int imgH = TILE_H + H;
        BufferedImage img = new BufferedImage(TILE_W, imgH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int hw = TILE_W / 2;
        int hh = TILE_H / 2;
        int tileW = TILE_W;
        int tileH = TILE_H;

        Color leftCol = shift(body, -28);
        Color rightCol = body;

        int[] lxs = { 0, hw, hw, 0 };
        int[] lys = { hh, tileH, tileH + H, H + hh };
        g.setColor(leftCol);
        g.fillPolygon(lxs, lys, 4);
        g.setColor(shift(leftCol, -25));
        g.drawPolygon(lxs, lys, 4);

        int[] rxs = { tileW, tileW, hw, hw };
        int[] rys = { hh, H + hh, H + tileH, tileH };
        g.setColor(rightCol);
        g.fillPolygon(rxs, rys, 4);
        g.setColor(shift(rightCol, -25));
        g.drawPolygon(rxs, rys, 4);

        int[] roofXs = { hw, tileW, hw, 0 };
        int[] roofYs = { 0, hh, tileH, hh };
        g.setColor(roof);
        g.fillPolygon(roofXs, roofYs, 4);
        g.setColor(shift(roof, -30));
        g.drawPolygon(roofXs, roofYs, 4);

        g.setColor(new Color(255, 230, 150, 220));
        int rows = Math.max(1, H / 22);
        for (int row = 0; row < rows; row++) {
            int wy = tileH + 8 + row * 22;
            if (wy + 6 > tileH + H - 4)
                break;
            g.fillRect(hw / 4 + 2, wy, 4, 5);
            g.fillRect(hw / 4 + 12, wy + (row % 2 == 0 ? 0 : 2), 4, 5);
            g.fillRect(hw + hw / 2 - 4, wy + (row % 2 == 0 ? 0 : 2), 4, 5);
            g.fillRect(hw + hw / 4, wy, 4, 5);
        }

        g.dispose();
        return img;
    }

    private static Color shift(Color c, int delta) {
        return new Color(clamp(c.getRed() + delta), clamp(c.getGreen() + delta), clamp(c.getBlue() + delta));
    }

    private static int clamp(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }
}
