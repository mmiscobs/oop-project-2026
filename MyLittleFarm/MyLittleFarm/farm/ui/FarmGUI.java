package farm.ui;

import farm.core.FarmMarket;
import farm.core.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;

public class FarmGUI extends JFrame {

    private static final Color BG_DARK   = new Color(0x2B1A0E);
    private static final Color BG_MID    = new Color(0x3D2510);
    private static final Color BG_PANEL  = new Color(0x4E3018);
    private static final Color BORDER_C  = new Color(0xC8922A);
    private static final Color TEXT_MAIN = new Color(0xF5E6C8);
    private static final Color TEXT_DIM  = new Color(0x8A7060);
    private static final Color GOLD      = new Color(0xF5C842);
    private static final Color GREEN_C   = new Color(0x2EA84A);
    private static final Color RED_C     = new Color(0xC0392B);
    private static final Color BLUE_C    = new Color(0x3A8DDE);
    private static final Color SOIL_C    = new Color(0x8B5E3C);

    private static final Color[] STAGE_COLORS = {
        new Color(0xB8860B), new Color(0x90EE90), new Color(0x32CD32),
        new Color(0xFFD700), new Color(0xD2691E), new Color(0x808080),
    };

    // [displayName, spriteKey, type, speedLabel]
    private static final String[][] CROP_OPTIONS = {
        {"Wheat",     "wheat",     "grain",     "Fast (3 days)"},
        {"Corn",      "corn",      "grain",     "Slow (6 days)"},
        {"Tomato",    "tomato",    "vegetable", "Medium (4 days)"},
        {"Potato",    "potato",    "vegetable", "Med-Slow (6 days)"},
        {"Apple",     "apple",     "orchard",   "Very Slow (9 days)"},
        {"Sunflower", "sunflower", "orchard",   "Med-Fast (4 days)"},
    };

    private final Farm farm;
    private final FarmMarket market;
    private int money;
    private int day;
    private int selectedPlot = -1;

    private final Map<String, EnumMap<GrowthStage, Image>> cropSprites = new HashMap<>();
    private final EnumMap<GrowthStage, Image> genericSprites = new EnumMap<>(GrowthStage.class);
    private Image tilledDirtImg;
    private Image grassImg;

    private final PlotPanel[] plotPanels;
    private JLabel dayLabel, moneyLabel, waterLabel;
    private JTextArea logArea;
    private JComboBox<String> cropTypeBox;
    private JLabel selectedLabel;
    private JButton plantBtn, tickBtn, harvestBtn, sellBtn;
    private JTextField sellQtyField;
    private JPanel marketPanel;

    public FarmGUI(int numPlots, int startMoney) {
        this.farm = new Farm(numPlots);
        this.market = new FarmMarket();
        this.money = startMoney;
        this.day = 1;
        this.plotPanels = new PlotPanel[numPlots];
        loadSprites();
        buildUI();
        refreshAll();
        setTitle("🌾 farm.core.Farm Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 680));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    private void loadSprites() {
        String[] stageNames = {"seed","sprout","growing","mature","harvested","dead"};
        GrowthStage[] stages = GrowthStage.values();
        String[] cropKeys = {"wheat","corn","tomato","potato","apple","sunflower"};
        for (String key : cropKeys) {
            EnumMap<GrowthStage, Image> stageMap = new EnumMap<>(GrowthStage.class);
            for (int i = 0; i < stages.length && i < stageNames.length; i++) {
                Image img = loadRes(key + "_" + stageNames[i] + ".png");
                if (img != null) stageMap.put(stages[i], img);
            }
            cropSprites.put(key, stageMap);
        }
        String[] fallback = {"plant_seed","plant_sprout","plant_growing","plant_mature","plant_harvested","plant_dead"};
        for (int i = 0; i < stages.length && i < fallback.length; i++) {
            Image img = loadRes(fallback[i] + ".png");
            if (img != null) genericSprites.put(stages[i], img);
        }
        tilledDirtImg = loadRes("tilled_dirt.png");
        grassImg = loadRes("grass.png");
    }
    private Image loadRes(String name) {
        try {
            URL url = getClass().getResource("/assets/" + name);
            if (url != null) return ImageIO.read(url).getScaledInstance(48, 48, Image.SCALE_SMOOTH);
        } catch (IOException ignored) {}
        return null;
    }
    private Image getSpriteFor(Crop crop, GrowthStage stage) {
        String key = getCropKey(crop);
        if (key != null) {
            EnumMap<GrowthStage, Image> m = cropSprites.get(key);
            if (m != null && m.containsKey(stage)) return m.get(stage);
        }
        return genericSprites.get(stage);
    }
    private String getCropKey(Crop crop) {
        if (crop == null) return null;
        String name = crop.getName().toLowerCase();
        for (String[] opt : CROP_OPTIONS)
            if (name.equals(opt[0].toLowerCase())) return opt[1];
        return switch (crop.getType()) {
            case "grain" -> "wheat";
            case "vegetable" -> "tomato";
            case "orchard" -> "apple";
            default -> null;
        };
    }
    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBackground(BG_DARK);
        root.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        setContentPane(root);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildCenter(), BorderLayout.CENTER);
        root.add(buildSidebar(), BorderLayout.EAST);
    }
    private JPanel buildHeader() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 6));
        p.setBackground(BG_MID);
        p.setBorder(BorderFactory.createMatteBorder(0,0,2,0, BORDER_C));
        JLabel title = new JLabel("🌾 FARM SIMULATOR");
        title.setFont(pixelFont(13f)); title.setForeground(GOLD);
        p.add(title);
        dayLabel = hudLabel("📅 Day: 1");
        moneyLabel = hudLabel("💰 $500");
        waterLabel = hudLabel("💧 100");
        p.add(dayLabel); p.add(moneyLabel); p.add(waterLabel);
        tickBtn = styledButton("⏩ Next Day", GREEN_C);
        tickBtn.addActionListener(e -> doTick());
        p.add(Box.createHorizontalStrut(20)); p.add(tickBtn);
        return p;
    }
    private JPanel buildCenter() {
        JPanel wrap = new JPanel(new BorderLayout(0, 8));
        wrap.setBackground(BG_DARK);
        int cols = Math.min(4, farm.getNumberOfPlots());
        int rows = (int) Math.ceil(farm.getNumberOfPlots() / (double) cols);
        JPanel grid = new JPanel(new GridLayout(rows, cols, 8, 8));
        grid.setBackground(BG_DARK);
        grid.setBorder(panelBorder("farm.core.Farm Plots"));
        for (int i = 0; i < farm.getNumberOfPlots(); i++) {
            PlotPanel pp = new PlotPanel(i);
            plotPanels[i] = pp;
            grid.add(pp);
        }
        wrap.add(grid, BorderLayout.CENTER);
        wrap.add(buildLog(), BorderLayout.SOUTH);
        return wrap;
    }
    private JPanel buildSidebar() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_DARK);
        p.setPreferredSize(new Dimension(245, 0));
        p.add(buildActionPanel());
        p.add(Box.createVerticalStrut(8));
        p.add(buildMarketPanel());
        return p;
    }
    private JPanel buildActionPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_MID);
        p.setBorder(panelBorder("Actions"));

        selectedLabel = new JLabel("Select a plot");
        selectedLabel.setFont(pixelFont(9f)); selectedLabel.setForeground(TEXT_DIM);
        selectedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(selectedLabel); p.add(vgap(6));

        p.add(rowLabel("Choose farm.core.Crop:"));
        String[] labels = new String[CROP_OPTIONS.length];
        for (int i = 0; i < CROP_OPTIONS.length; i++)
            labels[i] = CROP_OPTIONS[i][0] + "  [" + CROP_OPTIONS[i][3] + "]";
        cropTypeBox = new JComboBox<>(labels);
        style(cropTypeBox); p.add(cropTypeBox); p.add(vgap(8));

        plantBtn = styledButton("🌱 Plant", GREEN_C);
        plantBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        plantBtn.addActionListener(e -> doPlant());
        p.add(plantBtn); p.add(vgap(4));

        harvestBtn = styledButton("🌾 Harvest", GOLD);
        harvestBtn.setForeground(BG_DARK);
        harvestBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        harvestBtn.addActionListener(e -> doHarvest());
        p.add(harvestBtn); p.add(vgap(10));

        JButton harvestAllBtn = styledButton("🌾 Harvest All", new Color(0xB8860B));
        harvestAllBtn.setForeground(TEXT_MAIN);
        harvestAllBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        harvestAllBtn.addActionListener(e -> doHarvestAll());
        p.add(harvestAllBtn); p.add(vgap(10));


        p.add(rowLabel("Sell qty:"));
        sellQtyField = new JTextField("10");
        style(sellQtyField); p.add(sellQtyField); p.add(vgap(4));

        sellBtn = styledButton("💰 Sell Stock", new Color(0xE8A020));
        sellBtn.setForeground(BG_DARK);
        sellBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        sellBtn.addActionListener(e -> doSell());
        p.add(sellBtn); p.add(vgap(10));

        p.add(rowLabel("Apply Event:")); p.add(vgap(3));
        JPanel evRow1 = new JPanel(new GridLayout(1,2,4,0));
        evRow1.setBackground(BG_MID); evRow1.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));
        evRow1.add(miniEventBtn("🔥 Drought", FarmEvent.DROUGHT));
        evRow1.add(miniEventBtn("🐛 Pest", FarmEvent.PEST));
        p.add(evRow1); p.add(vgap(3));
        JPanel evRow2 = new JPanel(new GridLayout(1,2,4,0));
        evRow2.setBackground(BG_MID); evRow2.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));
        evRow2.add(miniEventBtn("🐦 Birds", FarmEvent.BIRD_ATTACK));
        evRow2.add(miniEventBtn("🌟 Fruitful", FarmEvent.FRUITFUL_HARVEST));
        p.add(evRow2); p.add(vgap(8));
        p.add(buildCropLegend());
        return p;
    }

    private JPanel buildCropLegend() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG_PANEL);
        p.setBorder(panelBorder("farm.core.Crop Guide"));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (String[] opt : CROP_OPTIONS) {
            JLabel l = new JLabel(opt[0] + ": " + opt[3]);
            l.setFont(monoFont(10f));
            l.setForeground(switch(opt[2]){
                case "grain" -> GOLD; case "vegetable" -> GREEN_C; default -> BLUE_C;
            });
            l.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(l);
        }
        return p;
    }

    private JPanel buildMarketPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_MID); p.setBorder(panelBorder("Market"));
        marketPanel = new JPanel();
        marketPanel.setLayout(new BoxLayout(marketPanel, BoxLayout.Y_AXIS));
        marketPanel.setBackground(BG_MID);
        JScrollPane scroll = new JScrollPane(marketPanel);
        scroll.setBorder(null); scroll.setBackground(BG_MID);
        scroll.getViewport().setBackground(BG_MID);
        scroll.setPreferredSize(new Dimension(220, 130));
        p.add(scroll, BorderLayout.CENTER);
        return p;
    }
    private JPanel buildLog() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_MID); p.setBorder(panelBorder("Log"));
        p.setPreferredSize(new Dimension(0, 110));
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logArea.setBackground(BG_DARK); logArea.setForeground(TEXT_MAIN);
        logArea.setCaretColor(TEXT_MAIN);
        p.add(new JScrollPane(logArea));
        return p;
    }
    private void doPlant() {
        if (selectedPlot < 0) { log("⚠ Select a plot first."); return; }
        int idx = cropTypeBox.getSelectedIndex();
        String[] opt = CROP_OPTIONS[idx];
        Crop crop = switch (opt[1]) {
            case "wheat" -> new WheatCrop();
            case "corn" -> new CornCrop();
            case "tomato" -> new TomatoCrop();
            case "potato" -> new PotatoCrop();
            case "apple" -> new AppleCrop();
            case "sunflower" -> new SunflowerCrop();
            default -> new WheatCrop();
        };
        farm.getPlot(selectedPlot).plant(crop);
        log("🌱 Planted " + opt[0] + " (" + opt[2] + ") in plot " + selectedPlot + " — " + opt[3] + ".");
        refreshAll();
    }

    private void doTick() {
        farm.tick(); day++;
        log("☀  Day " + day + " — all crops advanced.");
        for (int i = 0; i < farm.getNumberOfPlots(); i++) {
            FarmPlot p = farm.getPlot(i);
            if (p.getCrop() != null && p.getCrop().isReady())
                log("  ✔ Plot " + i + ": " + p.getCrop().getName() + " is MATURE!");
        }
        refreshAll();
    }

    private void doHarvest() {
        if (selectedPlot < 0) { log("⚠ Select a plot first."); return; }
        FarmPlot plot = farm.getPlot(selectedPlot);
        if (plot.getCrop() == null) { log("⚠ Plot " + selectedPlot + " is empty."); return; }
        String cropName = plot.getCrop().getName();
        int price = plot.getCrop().getMarketprice();
        int yield = plot.harvest();
        if (yield == 0) { log("⚠ Plot " + selectedPlot + ": crop not ready yet."); }
        else { market.addToInventory(cropName, yield, price); log("🌾 Harvested " + yield + " of " + cropName + " → market."); }
        refreshAll();
    }
    private void doHarvestAll() {
        int totalHarvested = 0;
        for (int i = 0; i < farm.getNumberOfPlots(); i++) {
            FarmPlot plot = farm.getPlot(i);
            if (plot.getCrop() != null && plot.getCrop().isReady()) {
                String cropName = plot.getCrop().getName();
                int price = plot.getCrop().getMarketprice();
                int yield = plot.harvest();
                if (yield > 0) {
                    market.addToInventory(cropName, yield, price);
                    log("🌾 Plot " + i + ": harvested " + yield + " of " + cropName + ".");
                    totalHarvested++;
                }
            }
        }
        if (totalHarvested == 0) log("⚠ No mature crops to harvest.");
        else log("✔ Harvested " + totalHarvested + " plots.");
        refreshAll();
    }

    private void doSell() {
        int qty;
        try { qty = Integer.parseInt(sellQtyField.getText().trim()); }
        catch (NumberFormatException e) { log("⚠ Enter a valid quantity."); return; }
        Map<String, Integer> inv = market.getInventory();
        if (inv.isEmpty()) { log("⚠ Market inventory is empty."); return; }
        int earned = 0;
        for (Map.Entry<String, Integer> entry : inv.entrySet()) {
            int toSell = Math.min(qty, entry.getValue());
            int sold = market.sellCrop(entry.getKey(), toSell);
            earned += sold;
            if (sold > 0) log("💰 Sold " + toSell + " of " + entry.getKey() + " for $" + sold);
        }
        if (earned > 0) { money += earned; log("   Total: $" + earned + "  Balance: $" + money); }
        else log("⚠ Not enough stock.");
        refreshAll();
    }

    private void doEvent(FarmEvent e) {
        if (selectedPlot < 0) { log("⚠ Select a plot first."); return; }
        FarmPlot plot = farm.getPlot(selectedPlot);
        if (plot.getCrop() == null) { log("⚠ No crop in plot " + selectedPlot + "."); return; }
        plot.applyEvent(e);
        log("⚡ Event " + e + " on plot " + selectedPlot + ".");
        refreshAll();
    }

    private void refreshAll() {
        dayLabel.setText("📅 Day: " + day);
        moneyLabel.setText("💰 $" + money);
        waterLabel.setText("💧 " + farm.getIrrigation().getWaterLevel());
        for (int i = 0; i < farm.getNumberOfPlots(); i++) plotPanels[i].refresh();
        refreshMarket();
    }
    private void refreshMarket() {
        marketPanel.removeAll();
        Map<String, Integer> inv = market.getInventory();
        if (inv.isEmpty()) {
            JLabel empty = new JLabel("  (no stock)");
            empty.setFont(monoFont(11f)); empty.setForeground(TEXT_DIM);
            marketPanel.add(empty);
        } else {
            JLabel hdr = new JLabel("  CROP          QTY    $/unit");
            hdr.setFont(monoFont(11f)); hdr.setForeground(GOLD);
            marketPanel.add(hdr);
            inv.forEach((name, qty) -> {
                int price = market.getPriceOf(name);
                JLabel row = new JLabel(String.format("  %-14s %-6d $%d", name, qty, price));
                row.setFont(monoFont(11f)); row.setForeground(TEXT_MAIN);
                marketPanel.add(row);
            });
        }
        marketPanel.revalidate(); marketPanel.repaint();
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private JButton miniEventBtn(String label, FarmEvent event) {
        JButton btn = new JButton(label);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        btn.setBackground(BG_PANEL); btn.setForeground(TEXT_MAIN);
        btn.setBorder(BorderFactory.createLineBorder(BORDER_C));
        btn.setFocusPainted(false);
        btn.addActionListener(e -> doEvent(event));
        return btn;
    }

    private Font pixelFont(float size) { return new Font("Monospaced", Font.BOLD, (int)size); }
    private Font monoFont(float size)  { return new Font("Monospaced", Font.PLAIN, (int)size); }

    private JLabel hudLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(monoFont(14f)); l.setForeground(TEXT_MAIN);
        l.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_C),
            BorderFactory.createEmptyBorder(2,8,2,8)));
        l.setBackground(BG_DARK); l.setOpaque(true);
        return l;
    }

    private JButton styledButton(String label, Color bg) {
        JButton btn = new JButton(label);
        btn.setFont(pixelFont(10f)); btn.setBackground(bg); btn.setForeground(TEXT_MAIN);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_C.darker()),
            BorderFactory.createEmptyBorder(4,10,4,10)));
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        return btn;
    }

    private void style(JComboBox<?> box) {
        box.setBackground(BG_PANEL); box.setForeground(TEXT_MAIN);
        box.setFont(monoFont(11f));
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        box.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void style(JTextField tf) {
        tf.setBackground(BG_PANEL); tf.setForeground(TEXT_MAIN);
        tf.setFont(monoFont(12f)); tf.setCaretColor(TEXT_MAIN);
        tf.setBorder(BorderFactory.createLineBorder(BORDER_C));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private JLabel rowLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(monoFont(11f)); l.setForeground(TEXT_DIM);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private Border panelBorder(String title) {
        TitledBorder tb = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_C), title);
        tb.setTitleFont(pixelFont(9f)); tb.setTitleColor(GOLD);
        return BorderFactory.createCompoundBorder(tb,
            BorderFactory.createEmptyBorder(4,6,4,6));
    }

    private Component vgap(int h) { return Box.createVerticalStrut(h); }

    private class PlotPanel extends JPanel {
        private final int index;
        PlotPanel(int index) {
            this.index = index;
            setPreferredSize(new Dimension(120,120));
            setBackground(SOIL_C);
            setBorder(BorderFactory.createLineBorder(BORDER_C.darker(), 2));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) { selectPlot(index); }
            });
        }

        void refresh() {
            setBorder(BorderFactory.createLineBorder(
                index == selectedPlot ? GOLD : BORDER_C.darker(), 2));
            repaint();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            int w = getWidth(), h = getHeight();
            FarmPlot plot = farm.getPlot(index);

            if (tilledDirtImg != null && plot.getCrop() != null)
                g2.drawImage(tilledDirtImg, 0, 0, w, h, null);
            else if (grassImg != null)
                g2.drawImage(grassImg, 0, 0, w, h, null);
            else {
                g2.setColor(plot.getCrop() != null ? SOIL_C : new Color(0x3A5C2A));
                g2.fillRect(0, 0, w, h);
            }

            if (plot.isIrrigated()) {
                g2.setColor(new Color(0x3A8DDE, false));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
                g2.fillRect(0, 0, w, h);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }

            if (plot.getCrop() == null) {
                drawCenteredString(g2,"Plot "+index, w/2, h/2-8, monoFont(11f), TEXT_DIM);
                drawCenteredString(g2,"(empty)", w/2, h/2+8, monoFont(10f), TEXT_DIM);
                drawSoilBar(g2, plot, w, h);
                return;
            }

            Crop crop = plot.getCrop();
            GrowthStage stage = crop.getStage();
            Image sprite = getSpriteFor(crop, stage);
            int ss = 75, sx = (w-ss)/2, sy = (h-ss)/2-6;
            if (sprite != null) g2.drawImage(sprite, sx, sy, ss, ss, null);
            else {
                int ci = stage.ordinal();
                g2.setColor(STAGE_COLORS[Math.min(ci, STAGE_COLORS.length-1)]);
                g2.fillOval(sx+8, sy+8, ss-16, ss-16);
            }

            drawCenteredString(g2, crop.getName(), w/2, h-22, monoFont(10f), TEXT_MAIN);

            int barW=w-12, barH=6, barX=6, barY=h-12;
            int ci=stage.ordinal(), total= GrowthStage.values().length-2;
            int filled=(int)((ci/(double)total)*barW);
            g2.setColor(BG_DARK); g2.fillRoundRect(barX,barY,barW,barH,3,3);
            g2.setColor(STAGE_COLORS[Math.min(ci,STAGE_COLORS.length-1)]);
            g2.fillRoundRect(barX,barY,Math.max(4,filled),barH,3,3);

            if (plot.getEvent() != null) {
                String badge = switch(plot.getEvent()){
                    case FarmEvent.DROUGHT->"🔥"; case FarmEvent.PEST->"🐛";
                    case FarmEvent.BIRD_ATTACK->"🐦"; case FarmEvent.FRUITFUL_HARVEST->"🌟";
                };
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
                g2.drawString(badge, w-22, 18);
            }
            if (plot.isIrrigated()) {
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
                g2.drawString("💧", 4, 18);
            }
            drawSoilBar(g2, plot, w, h);
        }

        private void drawSoilBar(Graphics2D g2, FarmPlot plot, int w, int h) {
            int qual = plot.getSoilQuality();
            g2.setColor(qual>60?GREEN_C:qual>30?GOLD:RED_C);
            g2.fillRect(0, 0, (int)(w*qual/100.0), 3);
        }

        private void drawCenteredString(Graphics2D g2, String s, int cx, int cy, Font f, Color c) {
            g2.setFont(f); g2.setColor(c);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(s, cx-fm.stringWidth(s)/2, cy+fm.getAscent()/2);
        }
    }

    private void selectPlot(int i) {
        selectedPlot = i;
        FarmPlot p = farm.getPlot(i);
        String info = p.getCrop()==null
            ? "Plot "+i+" (empty) — soil: "+p.getSoilQuality()+"%"
            : "Plot "+i+": "+p.getCrop().getName()+" ["+p.getCrop().getStage()+"] soil:"+p.getSoilQuality()+"%";
        selectedLabel.setText(info); selectedLabel.setForeground(GOLD);
        for (PlotPanel pp : plotPanels) pp.refresh();
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}
        int plots = args.length >= 1 ? Integer.parseInt(args[0]) : 6;
        int money = args.length >= 2 ? Integer.parseInt(args[1]) : 500;
        SwingUtilities.invokeLater(() -> new FarmGUI(plots, money));
    }
}
