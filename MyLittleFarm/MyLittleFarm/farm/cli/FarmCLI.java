package farm.cli;

import farm.core.*;

import java.util.Scanner;
import java.util.Map;

public class FarmCLI {
    private static final String RESET = "\u001B[0m";
    private static final String BOLD  = "\u001B[1m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String RED = "\u001B[31m";
    private static final String BLUE = "\u001B[34m";
    private static final String DIM = "\u001B[2m";
    private static final String MAGENTA = "\u001B[35m";

    private final Farm farm;
    private final FarmMarket market;
    private int money;
    private int day;
    private final Scanner scanner;

    public FarmCLI(int plots, int startMoney) {
        this.farm = new Farm(plots);
        this.market = new FarmMarket();
        this.money = startMoney;
        this.day = 1;
        this.scanner = new Scanner(System.in);
    }
    public void run() {
        printBanner();
        printHelp();
        while (true) {
            System.out.print(BOLD + GREEN + "\n> " + RESET);
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\s+");
            String cmd = parts[0].toLowerCase();
            try {
                switch (cmd) {
                    case "status" -> printStatus();
                    case "plant" -> cmdPlant(parts);
                    case "tick" -> cmdTick();
                    case "harvest" -> cmdHarvest(parts);
                    case "sell" -> cmdSell(parts);
                    case "market" -> printMarket();
                    case "event" -> cmdEvent(parts);
                    case "water" -> cmdWater(parts);
                    case "help" -> printHelp();
                    case "quit", "exit" -> { System.out.println(YELLOW + "Goodbye, farmer!" + RESET); return; }
                    default -> System.out.println(RED + "Unknown command. Type 'help'." + RESET);
                }
            } catch (Exception e) {
                System.out.println(RED + "Error: " + e.getMessage() + RESET);
            }
        }
    }

    private void cmdPlant(String[] parts) {
        if (parts.length < 4) { System.out.println(RED + "Usage: plant <plotIndex> <type> <cropName>" + RESET); return; }
        int idx  = Integer.parseInt(parts[1]);
        String type = parts[2].toLowerCase();
        String name = parts[3];
        validatePlotIndex(idx);

        Crop crop = switch (type) {
            case "grain" -> new GrainCrop(name);
            case "vegetable" -> new VegetableCrop(name);
            case "orchard" -> new OrchardCrop(name);
            default -> throw new IllegalArgumentException("Unknown type '" + type + "'. Use: grain | vegetable | orchard");
        };

        farm.getPlot(idx).plant(crop);
        System.out.printf(GREEN + "🌱 Planted %s (%s) in plot %d.%n" + RESET, name, type, idx);
    }
    private void cmdTick() {
        farm.tick();
        day++;
        System.out.printf(CYAN + "☀  Day %d — all plots irrigated and crops advanced.%n" + RESET, day);
        for (int i = 0; i < farm.getNumberOfPlots(); i++) {
            FarmPlot p = farm.getPlot(i);
            if (p.getCrop() != null && p.getCrop().isReady()) {
                System.out.printf(YELLOW + "  ✔ Plot %d: %s is MATURE and ready to harvest!%n" + RESET,
                        i, p.getCrop().getName());
            }
        }
    }
    private void cmdHarvest(String[] parts) {
        if (parts.length < 2) { System.out.println(RED + "Usage: harvest <plotIndex>" + RESET); return; }
        int idx = Integer.parseInt(parts[1]);
        validatePlotIndex(idx);
        FarmPlot plot = farm.getPlot(idx);
        if (plot.getCrop() == null) { System.out.println(RED + "Plot " + idx + " has no crop." + RESET); return; }
        String cropName = plot.getCrop().getName();
        String cropType = plot.getCrop().getType();
        int price = plot.getCrop().getMarketprice();
        int yield = plot.harvest();
        if (yield == 0) {
            System.out.printf(RED + "Plot %d: crop is not ready to harvest yet.%n" + RESET, idx);
        } else {
            market.addToInventory(cropName, yield, price);
            System.out.printf(YELLOW + "🌾 Harvested plot %d: %d units of %s (%s) → added to market inventory.%n" + RESET,
                    idx, yield, cropName, cropType);
        }
    }
    private void cmdSell(String[] parts) {
        if (parts.length < 3) { System.out.println(RED + "Usage: sell <cropName> <quantity>" + RESET); return; }
        String name = parts[1];
        int qty = Integer.parseInt(parts[2]);
        int stock = market.getInventory().getOrDefault(name, 0);
        if (stock == 0) { System.out.println(RED + "No stock of '" + name + "' in market." + RESET); return; }

        int toSell = Math.min(qty, stock);
        int earned = market.sellCrop(name, toSell);
        if (earned == 0) {
            System.out.printf(RED + "Not enough stock of %s (have %d, need %d).%n" + RESET, name, stock, qty);
        } else {
            money += earned;
            System.out.printf(GREEN + "\uD83D\uDCB0 Sold %d units of %s for $%d. Balance: $%d%n" + RESET,
                    toSell, name, earned, money);
        }
    }
    private void cmdEvent(String[] parts) {
        if (parts.length < 3) { System.out.println(RED + "Usage: event <plotIndex> <DROUGHT|PEST|BIRD_ATTACK|FRUITFUL_HARVEST>" + RESET); return; }
        int idx = Integer.parseInt(parts[1]);
        validatePlotIndex(idx);
        FarmEvent e = FarmEvent.valueOf(parts[2].toUpperCase());
        FarmPlot plot = farm.getPlot(idx);
        if (plot.getCrop() == null) { System.out.println(RED + "Plot " + idx + " has no crop to apply event to." + RESET); return; }
        plot.applyEvent(e);
        String icon = switch (e) {
            case FarmEvent.DROUGHT -> "🔥";
            case FarmEvent.PEST -> "🐛";
            case FarmEvent.BIRD_ATTACK -> "🐦";
            case FarmEvent.FRUITFUL_HARVEST -> "🌟";
        };
        System.out.printf(MAGENTA + "%s Event %s applied to plot %d (%s).%n" + RESET,
                icon, e, idx, plot.getCrop().getName());
    }

    private void cmdWater(String[] parts) {
        if (parts.length < 2) { System.out.println(RED + "Usage: water <plotIndex>" + RESET); return; }
        int idx = Integer.parseInt(parts[1]);
        validatePlotIndex(idx);
        farm.getIrrigation().irrigateOne(farm.getPlot(idx));
        System.out.printf(BLUE + "💧 Plot %d manually irrigated. Water level: %d%n" + RESET,
                idx, farm.getIrrigation().getWaterLevel());
    }

    private void printBanner() {
        System.out.println(GREEN + BOLD);
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║    🌾  FARM SIMULATOR  –  CLI MODE  🌾   ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.printf("  Day: %-4d   Money: $%d%n", day, money);
        System.out.println(RESET);
    }

    private void printStatus() {
        System.out.println(BOLD + CYAN);
        System.out.printf("  %-5s %-14s %-12s %-10s %-8s %-6s %-18s%n",
                "PLOT", "CROP", "TYPE", "STAGE", "SOIL%", "💧", "EVENT");
        System.out.println("  " + "─".repeat(75));
        System.out.print(RESET);

        FarmPlot[] plots = farm.getPlots();
        for (int i = 0; i < plots.length; i++) {
            FarmPlot p = plots[i];
            if (p.getCrop() == null) {
                System.out.printf(DIM + "  %-5d %-14s %-12s %-10s %-8d %-6s %-18s%n" + RESET,
                        i, "(empty)", "-", "-", p.getSoilQuality(),
                        p.isIrrigated() ? "✔" : " ", "-");
            } else {
                Crop c = p.getCrop();
                String stageColor = switch (c.getStage()) {
                    case SEED -> DIM;
                    case SPROUT -> GREEN;
                    case GROWING -> CYAN;
                    case MATURE -> YELLOW + BOLD;
                    case HARVESTED -> DIM;
                    case DEAD -> RED;
                };
                String eventStr = p.getEvent() == null ? "-" : p.getEvent().name();
                System.out.printf("  %-5d " + stageColor + "%-14s" + RESET + " %-12s " +
                                stageColor + "%-10s" + RESET + " %-8d %-6s %-18s%n",
                        i, c.getName(), c.getType(), c.getStage(),
                        p.getSoilQuality(), p.isIrrigated() ? "💧" : " ", eventStr);
            }
        }
        System.out.printf(BOLD + "\n  Day: %d   Money: $%d   Water: %d%n" + RESET,
                day, money, farm.getIrrigation().getWaterLevel());
    }
    private void printMarket() {
        System.out.println(BOLD + YELLOW + "\n  ─── Market Inventory ───" + RESET);
        Map<String, Integer> inv = market.getInventory();
        if (inv.isEmpty()) {
            System.out.println(DIM + "  (no crops in stock)" + RESET);
        } else {
            System.out.printf(BOLD + "  %-16s %-10s %-10s%n" + RESET, "CROP", "STOCK", "PRICE/UNIT");
            inv.forEach((name, qty) -> {
                int price = market.getPriceOf(name);
                System.out.printf("  %-16s %-10d $%-9d%n", name, qty, price);
            });
        }
        System.out.println();
        System.out.println(CYAN + "  Default prices:  grain=$5   vegetable=$15   orchard=$30" + RESET);
    }
    private void printHelp() {
        System.out.println(BOLD + CYAN + "\n  ─── Commands ───────────────────────────────────────────────────────" + RESET);
        String[][] cmds = {
            {"status",                         "Show all plots (crop, stage, soil, events)"},
            {"plant <plot> <type> <name>",      "Plant a crop  (type: grain | vegetable | orchard)"},
            {"tick",                            "Advance one day (irrigate + grow all crops)"},
            {"harvest <plot>",                  "Harvest a mature plot into market inventory"},
            {"sell <cropName> <qty>",           "Sell harvested stock for money"},
            {"market",                          "Show market inventory and prices"},
            {"event <plot> <EVENT>",            "Apply: DROUGHT | PEST | BIRD_ATTACK | FRUITFUL_HARVEST"},
            {"water <plot>",                    "Manually irrigate a single plot"},
            {"help",                            "Show this help"},
            {"quit",                            "Exit"},
        };
        for (String[] row : cmds) {
            System.out.printf("  " + GREEN + "%-36s" + RESET + " %s%n", row[0], row[1]);
        }
        System.out.println();
    }

    private void validatePlotIndex(int idx) {
        if (idx < 0 || idx >= farm.getNumberOfPlots())
            throw new IllegalArgumentException("Plot index out of range (0-" + (farm.getNumberOfPlots()-1) + ")");
    }

    private Crop findCropByName(String name) {
        for (FarmPlot p : farm.getPlots()) {
            if (p.getCrop() != null && p.getCrop().getName().equalsIgnoreCase(name))
                return p.getCrop();
        }
        return null;
    }


    public static void main(String[] args) {
        int plots = 6;
        int money = 500;
        if (args.length >= 1) plots = Integer.parseInt(args[0]);
        if (args.length >= 2) money = Integer.parseInt(args[1]);
        new FarmCLI(plots, money).run();
    }
}
