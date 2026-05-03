package ui;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IsometricMapView extends JComponent {

    public interface TileHoverListener {
        void onHover(int col, int row);
    }

    public interface TileClickListener {
        void onClick(int col, int row, MouseEvent e);
    }

    public interface TileDragListener {
        void onDragStart(int col, int row);
        void onDragMove(int startCol, int startRow, int currentCol, int currentRow);
        void onDragEnd(int startCol, int startRow, int endCol, int endRow);
    }

    public interface OverlayPainter {
        void paint(Graphics2D g, IsometricMapView view);
    }

    public enum TileAnchor { ON, ABOVE, BELOW, LEFT_OF, RIGHT_OF }

    private Image[][] tiles = new Image[0][0];
    private int cols = 0;
    private int rows = 0;

    private int tileW = 64;
    private int tileH = 32;

    private int originX = 0;
    private int originY = 0;

    private int hoverCol = -1;
    private int hoverRow = -1;
    private boolean drawHoverHighlight = true;

    private final List<TileHoverListener> hoverListeners = new ArrayList<>();
    private final List<TileClickListener> clickListeners = new ArrayList<>();
    private final List<TileDragListener> dragListeners = new ArrayList<>();
    private OverlayPainter overlay;

    private static final int DRAG_IDLE = 0;
    private static final int DRAG_ARMED = 1;
    private static final int DRAG_ACTIVE = 2;
    private int dragState = DRAG_IDLE;
    private int dragStartCol = -1;
    private int dragStartRow = -1;
    private int dragCurCol = -1;
    private int dragCurRow = -1;

    private static final class Attachment {
        int col, row;
        TileAnchor anchor;
        int offsetX, offsetY;
        Attachment(int c, int r, TileAnchor a, int ox, int oy) {
            col = c; row = r; anchor = a; offsetX = ox; offsetY = oy;
        }
    }
    private final Map<Component, Attachment> attachments = new LinkedHashMap<>();

    public IsometricMapView() {
        setLayout(null);
        setOpaque(true);
        setBackground(new Color(20, 24, 30));

        MouseAdapter m = new MouseAdapter() {
            @Override public void mouseMoved(MouseEvent e)   { updateHover(e.getX(), e.getY()); }
            @Override public void mouseExited(MouseEvent e)  {
                if (!contains(e.getPoint())) setHover(-1, -1);
            }
            @Override public void mouseClicked(MouseEvent e) {
                int[] cr = tileAt(e.getX(), e.getY());
                if (cr == null) return;
                for (TileClickListener l : clickListeners) l.onClick(cr[0], cr[1], e);
            }
            @Override public void mousePressed(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) return;
                int[] cr = tileAt(e.getX(), e.getY());
                if (cr == null) return;
                dragState = DRAG_ARMED;
                dragStartCol = cr[0]; dragStartRow = cr[1];
                dragCurCol   = cr[0]; dragCurRow   = cr[1];
            }
            @Override public void mouseDragged(MouseEvent e) {
                updateHover(e.getX(), e.getY());
                if (dragState == DRAG_IDLE) return;
                int[] cr = tileAt(e.getX(), e.getY());
                if (cr == null) return;
                if (cr[0] == dragCurCol && cr[1] == dragCurRow) return;
                if (dragState == DRAG_ARMED) {
                    dragState = DRAG_ACTIVE;
                    for (TileDragListener l : dragListeners) l.onDragStart(dragStartCol, dragStartRow);
                }
                dragCurCol = cr[0]; dragCurRow = cr[1];
                for (TileDragListener l : dragListeners) l.onDragMove(dragStartCol, dragStartRow, cr[0], cr[1]);
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) return;
                if (dragState == DRAG_ACTIVE) {
                    for (TileDragListener l : dragListeners) l.onDragEnd(dragStartCol, dragStartRow, dragCurCol, dragCurRow);
                }
                dragState = DRAG_IDLE;
            }
        };
        addMouseListener(m);
        addMouseMotionListener(m);
    }

    public void setTiles(Image[][] grid) {
        if (grid == null) {
            tiles = new Image[0][0];
            cols = rows = 0;
        } else {
            tiles = grid;
            rows = grid.length;
            cols = rows > 0 && grid[0] != null ? grid[0].length : 0;
        }
        revalidate();
        repaint();
    }

    public void setTile(int col, int row, Image image) {
        if (row < 0 || row >= rows || col < 0 || col >= cols) return;
        tiles[row][col] = image;
        repaint();
    }

    public void setTileSize(int tileWidth, int tileHeight) {
        if (tileWidth <= 0 || tileHeight <= 0) {
            throw new IllegalArgumentException("tile dimensions must be positive");
        }
        this.tileW = tileWidth;
        this.tileH = tileHeight;
        revalidate();
        repaint();
    }

    public void setOrigin(int x, int y) {
        this.originX = x;
        this.originY = y;
        revalidate();
        repaint();
    }

    public void setHoverHighlightVisible(boolean visible) {
        this.drawHoverHighlight = visible;
        repaint();
    }

    public int getColumns()    { return cols; }
    public int getRows()       { return rows; }
    public int getTileWidth()  { return tileW; }
    public int getTileHeight() { return tileH; }
    public int getHoveredCol() { return hoverCol; }
    public int getHoveredRow() { return hoverRow; }

    public void addTileHoverListener(TileHoverListener l)    { hoverListeners.add(l); }
    public void removeTileHoverListener(TileHoverListener l) { hoverListeners.remove(l); }
    public void addTileClickListener(TileClickListener l)    { clickListeners.add(l); }
    public void removeTileClickListener(TileClickListener l) { clickListeners.remove(l); }
    public void addTileDragListener(TileDragListener l)      { dragListeners.add(l); }
    public void removeTileDragListener(TileDragListener l)   { dragListeners.remove(l); }

    public void setOverlay(OverlayPainter o) {
        this.overlay = o;
        repaint();
    }

    public void drawTileDiamond(Graphics2D g, int col, int row, Color fill, Color stroke) {
        Point p = tileToScreen(col, row);
        int hw = tileW / 2;
        int hh = tileH / 2;
        int[] xs = { p.x,      p.x + hw, p.x,      p.x - hw };
        int[] ys = { p.y - hh, p.y,      p.y + hh, p.y };
        if (fill != null)   { g.setColor(fill);   g.fillPolygon(xs, ys, 4); }
        if (stroke != null) { g.setColor(stroke); g.drawPolygon(xs, ys, 4); }
    }

    public void attachComponent(Component comp, int col, int row, TileAnchor anchor) {
        attachComponent(comp, col, row, anchor, 0, 0);
    }

    public void attachComponent(Component comp, int col, int row, TileAnchor anchor, int offsetX, int offsetY) {
        if (comp == null || anchor == null) throw new IllegalArgumentException("null arg");
        Attachment prev = attachments.put(comp, new Attachment(col, row, anchor, offsetX, offsetY));
        if (prev == null) add(comp);
        revalidate();
        repaint();
    }

    public void moveAttachment(Component comp, int col, int row) {
        Attachment a = attachments.get(comp);
        if (a == null) return;
        a.col = col;
        a.row = row;
        revalidate();
        repaint();
    }

    public void detachComponent(Component comp) {
        if (attachments.remove(comp) != null) {
            remove(comp);
            revalidate();
            repaint();
        }
    }

    public Point tileToScreen(int col, int row) {
        int cx = originX + (col - row) * (tileW / 2);
        int cy = originY + (col + row) * (tileH / 2);
        return new Point(cx, cy);
    }

    public int[] tileAt(int sx, int sy) {
        double dx = sx - originX;
        double dy = sy - originY;
        double halfW = tileW / 2.0;
        double halfH = tileH / 2.0;
        double col = (dx / halfW + dy / halfH) / 2.0;
        double row = (dy / halfH - dx / halfW) / 2.0;
        int c = (int) Math.floor(col + 0.5);
        int r = (int) Math.floor(row + 0.5);
        if (c < 0 || r < 0 || c >= cols || r >= rows) return null;
        return new int[]{c, r};
    }

    private void updateHover(int sx, int sy) {
        int[] cr = tileAt(sx, sy);
        if (cr == null) setHover(-1, -1);
        else setHover(cr[0], cr[1]);
    }

    private void setHover(int col, int row) {
        if (col == hoverCol && row == hoverRow) return;
        hoverCol = col;
        hoverRow = row;
        for (TileHoverListener l : hoverListeners) l.onHover(col, row);
        if (drawHoverHighlight) repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        if (cols == 0 || rows == 0) return new Dimension(0, 0);
        int w = (cols + rows) * (tileW / 2) + tileW;
        int h = (cols + rows) * (tileH / 2) + tileH;
        return new Dimension(w, h);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            if (isOpaque()) {
                g2.setColor(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    drawTile(g2, c, r);
                }
            }
            if (drawHoverHighlight && hoverCol >= 0 && hoverRow >= 0) {
                drawHoverDiamond(g2, hoverCol, hoverRow);
            }
            if (overlay != null) overlay.paint(g2, this);
        } finally {
            g2.dispose();
        }
    }

    private void drawTile(Graphics2D g2, int col, int row) {
        Image img = tiles[row][col];
        if (img == null) return;
        Point p = tileToScreen(col, row);
        int iw = img.getWidth(this);
        int ih = img.getHeight(this);
        if (iw <= 0 || ih <= 0) return;
        int bottomX = p.x;
        int bottomY = p.y + tileH / 2;
        g2.drawImage(img, bottomX - iw / 2, bottomY - ih, this);
    }

    private void drawHoverDiamond(Graphics2D g2, int col, int row) {
        Point p = tileToScreen(col, row);
        int hw = tileW / 2;
        int hh = tileH / 2;
        int[] xs = { p.x,      p.x + hw, p.x,      p.x - hw };
        int[] ys = { p.y - hh, p.y,      p.y + hh, p.y };
        g2.setColor(new Color(255, 255, 255, 70));
        g2.fillPolygon(xs, ys, 4);
        g2.setColor(new Color(255, 255, 255, 200));
        g2.drawPolygon(xs, ys, 4);
    }

    @Override
    public void doLayout() {
        for (Map.Entry<Component, Attachment> e : attachments.entrySet()) {
            Component comp = e.getKey();
            Attachment a = e.getValue();
            Dimension d = comp.getPreferredSize();
            Point bounds = computeAttachmentBounds(a, d);
            comp.setBounds(bounds.x, bounds.y, d.width, d.height);
        }
    }

    private Point computeAttachmentBounds(Attachment a, Dimension d) {
        Point center = tileToScreen(a.col, a.row);
        int hw = tileW / 2;
        int hh = tileH / 2;
        int x, y;
        switch (a.anchor) {
            case ABOVE:
                x = center.x - d.width / 2;
                y = (center.y - hh) - d.height;
                break;
            case BELOW:
                x = center.x - d.width / 2;
                y = center.y + hh;
                break;
            case LEFT_OF:
                x = (center.x - hw) - d.width;
                y = center.y - d.height / 2;
                break;
            case RIGHT_OF:
                x = center.x + hw;
                y = center.y - d.height / 2;
                break;
            case ON:
            default:
                x = center.x - d.width / 2;
                y = center.y - d.height / 2;
                break;
        }
        return new Point(x + a.offsetX, y + a.offsetY);
    }
}
