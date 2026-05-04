package ui;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class IsometricMapView extends JComponent {

    public interface TileHoverListener {
        void onHover(utils.Point loc);
    }

    public interface TileClickListener {
        void onClick(utils.Point loc, MouseEvent e);
    }

    public interface TileDragListener {
        void onDragStart(utils.Point loc);

        void onDragMove(utils.Point from, utils.Point to);

        void onDragEnd(utils.Point from, utils.Point to);
    }

    public interface OverlayPainter {
        void paint(Graphics2D g, IsometricMapView view);
    }

    public enum TileAnchor {
        ON, ABOVE, BELOW, LEFT_OF, RIGHT_OF
    }

    public static final class Sprite {
        public final Image image;
        public final int anchorCol;
        public final int anchorRow;
        public final int footprintCols;
        public final int footprintRows;

        public Sprite(Image image, int anchorCol, int anchorRow, int footprintCols, int footprintRows) {
            if (image == null)
                throw new IllegalArgumentException("image is null");
            if (footprintCols <= 0 || footprintRows <= 0)
                throw new IllegalArgumentException("footprint must be positive");
            this.image = image;
            this.anchorCol = anchorCol;
            this.anchorRow = anchorRow;
            this.footprintCols = footprintCols;
            this.footprintRows = footprintRows;
        }

        int frontDepth() {
            return anchorCol + anchorRow + footprintCols + footprintRows - 2;
        }
    }

    private Image[][] tiles = new Image[0][0];
    private int cols = 0;
    private int rows = 0;

    private int tileW = 64;
    private int tileH = 32;
    private double zoom = 1.0;
    private int surfaceOffset = 0;

    private int originX = 0;
    private int originY = 0;

    private int hoverCol = -1;
    private int hoverRow = -1;
    private boolean drawHoverHighlight = true;

    private final List<TileHoverListener> hoverListeners = new Vector<>();
    private final List<TileClickListener> clickListeners = new Vector<>();
    private final List<TileDragListener> dragListeners = new Vector<>();
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
            col = c;
            row = r;
            anchor = a;
            offsetX = ox;
            offsetY = oy;
        }
    }

    private final Map<Component, Attachment> attachments = new LinkedHashMap<>();
    private final List<Sprite> sprites = new ArrayList<>();
    private final Map<Sprite, List<Sprite>> spriteChunks = new java.util.HashMap<>();

    private JPanel controls;
    private boolean controlsVisible = true;
    private double minZoom = 0.25;
    private double maxZoom = 8.0;
    private double zoomStep = 2;

    public IsometricMapView() {
        setLayout(null);
        setOpaque(true);
        setBackground(new Color(20, 24, 30));

        MouseAdapter m = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateHover(e.getX(), e.getY());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!contains(e.getPoint()))
                    setHover(-1, -1);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                int[] cr = tileAt(e.getX(), e.getY());
                if (cr == null)
                    return;
                for (TileClickListener l : clickListeners)
                    l.onClick(new utils.Point(cr[0], cr[1]), e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1)
                    return;
                int[] cr = tileAt(e.getX(), e.getY());
                if (cr == null)
                    return;
                dragState = DRAG_ARMED;
                dragStartCol = cr[0];
                dragStartRow = cr[1];
                dragCurCol = cr[0];
                dragCurRow = cr[1];
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                updateHover(e.getX(), e.getY());
                if (dragState == DRAG_IDLE)
                    return;
                int[] cr = tileAt(e.getX(), e.getY());
                if (cr == null)
                    return;
                if (cr[0] == dragCurCol && cr[1] == dragCurRow)
                    return;
                if (dragState == DRAG_ARMED) {
                    dragState = DRAG_ACTIVE;
                    for (TileDragListener l : dragListeners)
                        l.onDragStart(new utils.Point(dragStartCol, dragStartRow));
                }
                dragCurCol = cr[0];
                dragCurRow = cr[1];
                for (TileDragListener l : dragListeners)
                    l.onDragMove(new utils.Point(dragStartCol, dragStartRow), new utils.Point(cr[0], cr[1]));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1)
                    return;
                if (dragState == DRAG_ACTIVE) {
                    for (TileDragListener l : dragListeners)
                        l.onDragEnd(new utils.Point(dragStartCol, dragStartRow),
                                new utils.Point(dragCurCol, dragCurRow));
                }
                dragState = DRAG_IDLE;
            }
        };
        addMouseListener(m);
        addMouseMotionListener(m);

        setFocusable(true);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
            }
        });

        buildControls();
        installKeyBindings();
    }

    private void installKeyBindings() {
        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "iso.panUp");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "iso.panDown");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "iso.panLeft");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "iso.panRight");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0), "iso.zoomIn");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, KeyEvent.SHIFT_DOWN_MASK), "iso.zoomIn");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0), "iso.zoomIn");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD, 0), "iso.zoomIn");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "iso.zoomOut");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT, 0), "iso.zoomOut");

        am.put("iso.panUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panBy(0, panStepY());
            }
        });
        am.put("iso.panDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panBy(0, -panStepY());
            }
        });
        am.put("iso.panLeft", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panBy(panStepX(), 0);
            }
        });
        am.put("iso.panRight", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                panBy(-panStepX(), 0);
            }
        });
        am.put("iso.zoomIn", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoomBy(zoomStep);
            }
        });
        am.put("iso.zoomOut", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                zoomBy(1.0 / zoomStep);
            }
        });
    }

    private void buildControls() {
        controls = new JPanel(null);
        controls.setOpaque(false);

        JButton zoomIn = makeControlButton("+");
        JButton zoomOut = makeControlButton("−");
        JButton up = makeControlButton("▲");
        JButton down = makeControlButton("▼");
        JButton left = makeControlButton("◀");
        JButton right = makeControlButton("▶");

        zoomIn.addActionListener(e -> zoomBy(zoomStep));
        zoomOut.addActionListener(e -> zoomBy(1.0 / zoomStep));
        up.addActionListener(e -> panBy(0, panStepY()));
        down.addActionListener(e -> panBy(0, -panStepY()));
        left.addActionListener(e -> panBy(panStepX(), 0));
        right.addActionListener(e -> panBy(-panStepX(), 0));

        int btn = 26, gap = 2, sep = 8;
        zoomIn.setBounds(btn + gap, 0, btn, btn);
        zoomOut.setBounds(btn + gap, btn + gap, btn, btn);
        int compY = (btn + gap) * 2 + sep;
        up.setBounds(btn + gap, compY, btn, btn);
        left.setBounds(0, compY + btn + gap, btn, btn);
        right.setBounds((btn + gap) * 2, compY + btn + gap, btn, btn);
        down.setBounds(btn + gap, compY + (btn + gap) * 2, btn, btn);

        int totalW = (btn + gap) * 2 + btn;
        int totalH = compY + (btn + gap) * 2 + btn;
        controls.setSize(totalW, totalH);

        controls.add(zoomIn);
        controls.add(zoomOut);
        controls.add(up);
        controls.add(down);
        controls.add(left);
        controls.add(right);

        add(controls);
    }

    private JButton makeControlButton(String label) {
        JButton b = new JButton(label);
        b.setUI(new BasicButtonUI());
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setFocusable(false);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 14f));
        b.setBackground(new Color(35, 38, 50));
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 90)));
        b.setRolloverEnabled(true);
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        return b;
    }

    private int panStepX() {
        return Math.max(8, (int) Math.round(tileW * zoom));
    }

    private int panStepY() {
        return Math.max(8, (int) Math.round(tileH * zoom));
    }

    public void panBy(int dx, int dy) {
        setOrigin(originX + dx, originY + dy);
    }

    public void zoomBy(double factor) {
        double next = zoom * factor;
        next = Math.max(minZoom, Math.min(maxZoom, next));
        if (next != zoom)
            setZoom(next);
    }

    public void setZoomLimits(double min, double max) {
        if (min <= 0 || max < min)
            throw new IllegalArgumentException("invalid zoom range");
        this.minZoom = min;
        this.maxZoom = max;
        if (zoom < min || zoom > max) {
            setZoom(Math.max(min, Math.min(max, zoom)));
        }
    }

    public void setControlsVisible(boolean visible) {
        this.controlsVisible = visible;
        if (controls != null)
            controls.setVisible(visible);
        revalidate();
    }

    public boolean isControlsVisible() {
        return controlsVisible;
    }

    public JPanel getControlsPanel() {
        return controls;
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
        if (row < 0 || row >= rows || col < 0 || col >= cols)
            return;
        tiles[row][col] = image;
        repaint();
    }

    public void setTileSize(int tileWidth, int tileHeight) {
        if (tileWidth <= 0 || tileHeight <= 0) {
            throw new IllegalArgumentException("tile dimensions must be positive");
        }
        this.tileW = tileWidth;
        this.tileH = tileHeight;
        spriteChunks.clear();
        revalidate();
        repaint();
    }

    public void setOrigin(int x, int y) {
        this.originX = x;
        this.originY = y;
        revalidate();
        repaint();
    }

    public void setZoom(double zoom) {
        if (zoom <= 0 || !Double.isFinite(zoom)) {
            throw new IllegalArgumentException("zoom must be finite and positive");
        }
        this.zoom = zoom;
        revalidate();
        repaint();
    }

    public double getZoom() {
        return zoom;
    }

    public void setTileSurfaceOffset(int offsetPixels) {
        this.surfaceOffset = offsetPixels;
        revalidate();
        repaint();
    }

    public int getTileSurfaceOffset() {
        return surfaceOffset;
    }

    private int halfW() {
        return (int) Math.round(tileW * zoom / 2.0);
    }

    private int halfH() {
        return (int) Math.round(tileH * zoom / 2.0);
    }

    private int surfaceOffsetPx() {
        return (int) Math.round(surfaceOffset * zoom);
    }

    public void setHoverHighlightVisible(boolean visible) {
        this.drawHoverHighlight = visible;
        repaint();
    }

    public int getColumns() {
        return cols;
    }

    public int getRows() {
        return rows;
    }

    public int getTileWidth() {
        return tileW;
    }

    public int getTileHeight() {
        return tileH;
    }

    public int getHoveredCol() {
        return hoverCol;
    }

    public int getHoveredRow() {
        return hoverRow;
    }

    public void addTileHoverListener(TileHoverListener l) {
        hoverListeners.add(l);
    }

    public void removeTileHoverListener(TileHoverListener l) {
        hoverListeners.remove(l);
    }

    public void addTileClickListener(TileClickListener l) {
        clickListeners.add(l);
    }

    public void removeTileClickListener(TileClickListener l) {
        clickListeners.remove(l);
    }

    public void addTileDragListener(TileDragListener l) {
        dragListeners.add(l);
    }

    public void removeTileDragListener(TileDragListener l) {
        dragListeners.remove(l);
    }

    public void setOverlay(OverlayPainter o) {
        this.overlay = o;
        repaint();
    }

    public void drawTileDiamond(Graphics2D g, int col, int row, Color fill, Color stroke) {
        Point p = tileToScreen(col, row);
        int hw = halfW();
        int hh = halfH();
        int cy = p.y - surfaceOffsetPx();
        int[] xs = { p.x, p.x + hw, p.x, p.x - hw };
        int[] ys = { cy - hh, cy, cy + hh, cy };
        if (fill != null) {
            g.setColor(fill);
            g.fillPolygon(xs, ys, 4);
        }
        if (stroke != null) {
            g.setColor(stroke);
            g.drawPolygon(xs, ys, 4);
        }
    }

    public void attachComponent(Component comp, int col, int row, TileAnchor anchor) {
        attachComponent(comp, col, row, anchor, 0, 0);
    }

    public void attachComponent(Component comp, int col, int row, TileAnchor anchor, int offsetX, int offsetY) {
        if (comp == null || anchor == null)
            throw new IllegalArgumentException("null arg");
        Attachment prev = attachments.put(comp, new Attachment(col, row, anchor, offsetX, offsetY));
        if (prev == null) {
            add(comp);
            if (controls != null)
                setComponentZOrder(controls, 0);
        }
        revalidate();
        repaint();
    }

    public void moveAttachment(Component comp, int col, int row) {
        Attachment a = attachments.get(comp);
        if (a == null)
            return;
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

    public void addSprite(Sprite s) {
        if (s == null)
            throw new IllegalArgumentException("sprite is null");
        sprites.add(s);
        repaint();
    }

    public boolean removeSprite(Sprite s) {
        boolean removed = sprites.remove(s);
        if (removed) {
            spriteChunks.remove(s);
            repaint();
        }
        return removed;
    }

    public void clearSprites() {
        if (sprites.isEmpty())
            return;
        sprites.clear();
        spriteChunks.clear();
        repaint();
    }

    private List<Sprite> chunksFor(Sprite s) {
        if (s.footprintCols == 1 && s.footprintRows == 1)
            return Collections.singletonList(s);
        if (s.footprintCols == 2 && s.footprintRows == 2) {
            List<Sprite> cached = spriteChunks.get(s);
            if (cached != null)
                return cached;
            List<Sprite> chunks = splitTwoByTwo(s);
            spriteChunks.put(s, chunks);
            return chunks;
        }
        return Collections.singletonList(s);
    }

    private List<Sprite> splitTwoByTwo(Sprite s) {
        BufferedImage src = toBufferedImage(s.image);
        int imgW = src.getWidth();
        int imgH = src.getHeight();
        int hW = tileW / 2;
        int hH = tileH / 2;
        int diamondW = 4 * hW;
        int padLeft = (imgW - diamondW) / 2;

        int sideH = Math.max(1, imgH - hH);

        BufferedImage left = new BufferedImage(2 * hW, sideH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gl = left.createGraphics();
        gl.drawImage(src,
                0, 0, hW, sideH,
                Math.max(0, padLeft), 0, padLeft + hW, sideH,
                null);
        gl.dispose();

        BufferedImage middle = new BufferedImage(2 * hW, imgH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gm = middle.createGraphics();
        gm.drawImage(src,
                0, 0, 2 * hW, imgH,
                padLeft + hW, 0, padLeft + 3 * hW, imgH,
                null);
        gm.dispose();

        BufferedImage right = new BufferedImage(2 * hW, sideH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = right.createGraphics();
        gr.drawImage(src,
                hW, 0, 2 * hW, sideH,
                padLeft + 3 * hW, 0, padLeft + 4 * hW - 3, sideH,
                null);
        gr.dispose();

        int aCol = s.anchorCol;
        int aRow = s.anchorRow;
        return java.util.Arrays.asList(
                new Sprite(left, aCol, aRow + 1, 1, 1),
                new Sprite(middle, aCol + 1, aRow + 1, 1, 1),
                new Sprite(right, aCol + 1, aRow, 1, 1));
    }

    private static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage)
            return (BufferedImage) img;
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        BufferedImage bi = new BufferedImage(Math.max(1, w), Math.max(1, h), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return bi;
    }

    public List<Sprite> getSprites() {
        return Collections.unmodifiableList(sprites);
    }

    public Point footprintBottomCenter(int anchorCol, int anchorRow, int footprintCols, int footprintRows) {
        double halfWZ = tileW * zoom / 2.0;
        double halfHZ = tileH * zoom / 2.0;
        double cx = (anchorCol - anchorRow) * halfWZ
                + (footprintCols - footprintRows) * halfWZ / 2.0;
        double cy = (anchorCol + anchorRow + footprintCols + footprintRows - 1) * halfHZ;
        return new Point(originX + (int) Math.round(cx), originY + (int) Math.round(cy));
    }

    public Point tileToScreen(int col, int row) {
        int cx = originX + (col - row) * halfW();
        int cy = originY + (col + row) * halfH();
        return new Point(cx, cy);
    }

    public int[] tileAt(int sx, int sy) {
        double dx = sx - originX;
        double dy = sy - originY + surfaceOffsetPx();
        double halfW = tileW * zoom / 2.0;
        double halfH = tileH * zoom / 2.0;
        double col = (dx / halfW + dy / halfH) / 2.0;
        double row = (dy / halfH - dx / halfW) / 2.0;
        int c = (int) Math.floor(col + 0.5);
        int r = (int) Math.floor(row + 0.5);
        if (c < 0 || r < 0 || c >= cols || r >= rows)
            return null;
        return new int[] { c, r };
    }

    private void updateHover(int sx, int sy) {
        int[] cr = tileAt(sx, sy);
        if (cr == null)
            setHover(-1, -1);
        else
            setHover(cr[0], cr[1]);
    }

    private void setHover(int col, int row) {
        if (col == hoverCol && row == hoverRow)
            return;
        hoverCol = col;
        hoverRow = row;
        for (TileHoverListener l : hoverListeners)
            l.onHover(new utils.Point(col, row));
        if (drawHoverHighlight)
            repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        if (cols == 0 || rows == 0)
            return new Dimension(0, 0);
        int hw = halfW();
        int hh = halfH();
        int w = (cols + rows) * hw + 2 * hw;
        int h = (cols + rows) * hh + 2 * hh;
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
            paintWorld(g2);
            if (drawHoverHighlight && hoverCol >= 0 && hoverRow >= 0) {
                drawHoverDiamond(g2, hoverCol, hoverRow);
            }
            if (overlay != null)
                overlay.paint(g2, this);
        } finally {
            g2.dispose();
        }
    }

    private void paintWorld(Graphics2D g2) {
        List<Sprite> sorted;
        if (sprites.isEmpty()) {
            sorted = Collections.emptyList();
        } else {
            sorted = new ArrayList<>(sprites.size());
            for (Sprite s : sprites)
                sorted.addAll(chunksFor(s));
            sorted.sort(Comparator.comparingInt(Sprite::frontDepth));
        }
        int spriteIdx = 0;
        int maxDepth = (cols == 0 || rows == 0) ? -1 : (cols - 1) + (rows - 1);
        for (int d = 0; d <= maxDepth; d++) {
            int cMin = Math.max(0, d - rows + 1);
            int cMax = Math.min(cols - 1, d);
            for (int c = cMin; c <= cMax; c++) {
                drawTile(g2, c, d - c);
            }
            while (spriteIdx < sorted.size() && sorted.get(spriteIdx).frontDepth() == d) {
                drawSprite(g2, sorted.get(spriteIdx));
                spriteIdx++;
            }
        }
        while (spriteIdx < sorted.size()) {
            drawSprite(g2, sorted.get(spriteIdx));
            spriteIdx++;
        }
    }

    private void drawSprite(Graphics2D g, Sprite s) {
        int iw = s.image.getWidth(this);
        int ih = s.image.getHeight(this);
        if (iw <= 0 || ih <= 0)
            return;
        int sw = (int) Math.round(iw * zoom);
        int sh = (int) Math.round(ih * zoom);
        Point a = footprintBottomCenter(s.anchorCol, s.anchorRow, s.footprintCols, s.footprintRows);
        g.drawImage(s.image, a.x - sw / 2, a.y - sh, sw, sh, this);
    }

    private void drawTile(Graphics2D g2, int col, int row) {
        Image img = tiles[row][col];
        if (img == null)
            return;
        Point p = tileToScreen(col, row);
        int iw = img.getWidth(this);
        int ih = img.getHeight(this);
        if (iw <= 0 || ih <= 0)
            return;
        int sw = (int) Math.round(iw * zoom);
        int sh = (int) Math.round(ih * zoom);
        int bottomX = p.x;
        int bottomY = p.y + halfH();
        g2.drawImage(img, bottomX - sw / 2, bottomY - sh, sw, sh, this);
    }

    private void drawHoverDiamond(Graphics2D g2, int col, int row) {
        Point p = tileToScreen(col, row);
        int hw = halfW();
        int hh = halfH();
        int cy = p.y - surfaceOffsetPx();
        int[] xs = { p.x, p.x + hw, p.x, p.x - hw };
        int[] ys = { cy - hh, cy, cy + hh, cy };
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
        if (controls != null && controlsVisible) {
            int margin = 8;
            controls.setLocation(getWidth() - controls.getWidth() - margin, margin);
        }
    }

    private Point computeAttachmentBounds(Attachment a, Dimension d) {
        Point center = tileToScreen(a.col, a.row);
        center.y -= surfaceOffsetPx();
        int hw = halfW();
        int hh = halfH();
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
