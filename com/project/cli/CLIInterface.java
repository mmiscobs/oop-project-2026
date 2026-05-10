package com.project.cli;

import com.project.buildings.Buildable;
import com.project.buildings.Buildable.UnregisteredBuildingType;
import com.project.buildings.privatebuilding.residential.ResidentialBuilding;
import com.project.buildings.privatebuilding.workplace.WorkplaceBuilding;
import com.project.buildings.privatebuilding.workplace.commercial.CommercialBuilding;
import com.project.buildings.privatebuilding.workplace.industrial.IndustrialBuilding;
import com.project.buildings.privatebuilding.workplace.office.OfficeBuilding;
import com.project.buildings.publicbuilding.PublicBuilding;
import com.project.buildings.publicbuilding.transportation.PublicTransportation;
import com.project.city.City;
import com.project.loans.FederalLoan;
import com.project.loans.Loan;
import com.project.loans.PrivateLoan;
import com.project.simulation.Game;
import com.project.simulation.GameDifficulty;
import com.project.simulation.GameSpeed;
import com.project.simulation.Simulator;
import com.project.utils.Point;
import java.io.IOException;
import java.util.*;

public class CLIInterface {
    public static void main(String[] args) {
        run();
    }

    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String CYAN = "\u001B[36m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String GREY = "\u001B[90m";
    private static final String WHITE = "\u001B[97m";

    private static String c(String colour, String text) {
        return colour + text + RESET;
    }

    private static String iconFor(Buildable b) {
        if (b == null)
            return "  ";
        String name = b.getClass().getSimpleName();
        return switch (name) {
            case "Road" -> c(GREY, "Rd");
            case "Street" -> c(GREY, "St");
            case "SmallHouse" -> c(GREEN, "Ho");
            case "Condominium" -> c(GREEN, "Co");
            case "GroceryStore" -> c(YELLOW, "Gr");
            case "Mall" -> c(YELLOW, "Ma");
            case "BankBranch" -> c(CYAN, "Bk");
            case "Skyrise" -> c(CYAN, "Sk");
            case "Factory" -> c(RED, "Fa");
            case "Warehouse" -> c(RED, "Wh");
            case "Hospital" -> c(WHITE, "H+");
            case "Clinic" -> c(WHITE, "C+");
            case "BigPoliceStation" -> c(WHITE, "PB");
            case "SmallPoliceStation" -> c(WHITE, "Ps");
            default -> c(CYAN, name.substring(0, Math.min(2, name.length())));
        };
    }

    private static void printMap(Simulator sim) {
        City city = sim.city;
        int rows = city.grid.sizeY;
        int cols = city.grid.sizeX;

        System.out.println();
        System.out.println(c(BOLD, "  City map   tick " + sim.currentTickView.get() + "   " + city.name + "   $"
                + (int) (double) city.moneyView.get()));
        System.out.println();

        System.out.print("     ");
        for (int x = 1; x <= cols; x++) {
            System.out.printf(GREY + "%2d " + RESET, x);
        }
        System.out.println();
        System.out.print("    +");
        System.out.println(GREY + "--+".repeat(cols) + RESET);

        for (int y = 1; y <= rows; y++) {
            System.out.printf(GREY + "%2d" + RESET + "  |", y);
            for (int x = 1; x <= cols; x++) {
                Buildable b = city.grid.getBuildingAt(new Point(y - 1, x - 1));
                if (b != null) {
                    System.out.print(iconFor(b) + GREY + "|" + RESET);
                } else {
                    System.out.print(GREY + "··|" + RESET);
                }
            }
            System.out.println();
            System.out.print("    +");
            System.out.println(GREY + "--+".repeat(cols) + RESET);
        }

        System.out.println();
        System.out.println(c(GREY, "Legend: ")
                + c(GREEN, "Ho") + " Residential  "
                + c(YELLOW, "Gr") + " Commercial  "
                + c(CYAN, "Bk") + " Office  "
                + c(RED, "Fa") + " Industrial  "
                + c(WHITE, "H+") + " Service  "
                + c(GREY, "Rd") + " Transport");
        System.out.println(c(GREY, "Coords: x=column, y=row"));
        System.out.println();
    }

    private static void printStats(Simulator sim) {
        System.out.println();
        System.out.println(c(BOLD + CYAN, "═══ City Statistics ═══"));
        System.out.printf("  %-22s %s%n", "Tick:", sim.currentTickView.get());
        System.out.printf("  %-22s $%d%n", "Money:", (int) (double) sim.city.moneyView.get());
        System.out.printf("  %-22s %s%n", "Population:", sim.citizensAmountView.get());
        System.out.printf("  %-22s %s%n", "Homeless:", sim.homelessCitizensAmountView.get());
        System.out.printf("  %-22s $%.0f%n", "Total Debt:", sim.totalDebtView.get());
        System.out.println();
        System.out.println(c(BOLD, "  Income / Upkeep"));
        System.out.printf("  %-22s -$%s%n", "Upkeep:", sim.lastBuildingsUpkeepView.get());
        System.out.printf("  %-22s +$%s%n", "Business Tax:", sim.lastBusinessTaxView.get());
        System.out.printf("  %-22s +$%s%n", "Purchase Tax:", sim.lastPurchaseTaxView.get());
        System.out.printf("  %-22s +$%s%n", "Resident Tax:", sim.lastResidentTaxView.get());
        System.out.printf("  %-22s -$%.0f%n", "Loan Service:", sim.lastLoansServiceView.get());
        System.out.printf("  %-22s $%s%n", "Net Income:", sim.netIncomeView.get());
        System.out.println();
        System.out.println(c(BOLD, "  Citizen Wellbeing"));
        System.out.printf("  %-22s %s%n", "Avg Health:", sim.averageCitizenHealth());
        System.out.printf("  %-22s %s%n", "Avg Crime Rate:", sim.averageCrimeRate());
        System.out.printf("  %-22s %s%n", "Avg Satisfaction:", sim.averageCitizenSatisfaction());
        System.out.println();
        System.out.println(c(BOLD, "  Demand"));
        System.out.printf("  %-22s %s%n", "Residential:", ResidentialBuilding.calculateDemand(sim.city));
        System.out.printf("  %-22s %s%n", "Commercial:", CommercialBuilding.calculateDemand(sim.city));
        System.out.printf("  %-22s %s%n", "Office:", OfficeBuilding.calculateDemand(sim.city));
        System.out.printf("  %-22s %s%n", "Industrial:", IndustrialBuilding.calculateDemand(sim.city));
        System.out.println();
        System.out.println(c(BOLD, "  Shortages"));
        System.out.printf("  %-22s %.1f%%%n", "Housing:",
                (ResidentialBuilding.calculateHousingShortage(sim.city) * 100 - 100));
        System.out.printf("  %-22s %.1f%%%n", "Labor:",
                (WorkplaceBuilding.calculateLaborShortage(sim.city) * 100 - 100));
        System.out.printf("  %-22s %.1f%%%n", "Retail:",
                (CommercialBuilding.calculateRetailShortage(sim.city) * 100 - 100));
        System.out.println();
    }

    private static List<Class<? extends Buildable>> buildableList() {
        List<Class<? extends Buildable>> result = new ArrayList<>();
        for (Class<? extends Buildable> cat : List.of(
                PublicTransportation.class,
                ResidentialBuilding.class,
                CommercialBuilding.class,
                IndustrialBuilding.class,
                OfficeBuilding.class,
                com.project.buildings.publicbuilding.service.healthcare.HealthcareBuilding.class,
                com.project.buildings.publicbuilding.service.police.PoliceStation.class))
            for (Class<? extends Buildable> cls : Buildable.registry.keySet())
                if (cat.isAssignableFrom(cls))
                    result.add(cls);
        return result;
    }

    private static void printBuildingList() {
        List<Class<? extends Buildable>> list = buildableList();
        System.out.println();
        System.out.println(c(BOLD + CYAN, "═══ Available Buildings ═══"));
        for (int i = 0; i < list.size(); i++) {
            try {
                Buildable example = Buildable.createBuilding(list.get(i));
                String pretty = list.get(i).getSimpleName().replaceAll("[A-Z]", " $0").trim();
                System.out.printf("  [%2d]  %-28s  $%-6d  %dx%d tiles%n",
                        i, pretty, example.getPrice(), example.getLength(), example.getWidth());
            } catch (UnregisteredBuildingType e) {
                System.out.printf("  [%2d]  %s%n", i, list.get(i).getSimpleName());
            }
        }
        System.out.println();
        System.out.println(c(GREY, "  Usage: build <index> <x> <y>   (x=column, y=row)"));
        System.out.println();
    }

    private static void printHelp(boolean inGame) {
        System.out.println();
        if (!inGame) {
            System.out.println(c(BOLD + CYAN, "═══ Main Menu Commands ═══"));
            System.out.println("  new              start new-game wizard");
            System.out.println("  load             list saves and load one");
            System.out.println("  credits          show credits");
            System.out.println("  help             show this help");
            System.out.println("  exit             quit");
        } else {
            System.out.println(c(BOLD + CYAN, "═══ In-Game Commands ═══"));
            System.out.println("  map                   redraw the city grid");
            System.out.println("  stats                 show all statistics");
            System.out.println("  buildings             list building types with their index");
            System.out.println("  build <idx> <x> <y>   place a building  (x=column, y=row)");
            System.out.println("  demolish <x> <y>      demolish building at tile");
            System.out.println("  upgrade <x> <y>       show / apply upgrades");
            System.out.println("  info <x> <y>          detailed info for a tile");
            System.out.println("  speed <1-3>           set simulation speed (1=Slow 2=Normal 3=Fast)");
            System.out.println("  pause / resume        pause or resume ticks");
            System.out.println("  loan federal          take a federal loan");
            System.out.println("  loan private          take a private loan");
            System.out.println("  loans                 list active loans");
            System.out.println("  payout <idx>          pay out a loan (must have sufficient funds)");
            System.out.println("  save                  save the game");
            System.out.println("  menu                  return to main menu");
            System.out.println("  help                  show this help");
        }
        System.out.println();
    }

    private static boolean handleSimCommand(String line, Simulator sim, Game game, Scanner sc) {
        String[] parts = line.trim().split("\\s+");
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "map" -> printMap(sim);
            case "stats" -> printStats(sim);
            case "buildings" -> printBuildingList();
            case "build" -> {
                if (parts.length < 4) {
                    System.out.println(c(RED, "  Usage: build <index> <x> <y>   (x=column, y=row)"));
                    break;
                }
                try {
                    int idx = Integer.parseInt(parts[1]);
                    int x = Integer.parseInt(parts[2]);
                    int y = Integer.parseInt(parts[3]);
                    List<Class<? extends Buildable>> list = buildableList();
                    if (idx < 0 || idx >= list.size()) {
                        System.out.println(c(RED, "  Bad index. Run 'buildings' to see the list."));
                        break;
                    }
                    Buildable b = Buildable.createBuilding(list.get(idx));
                    if (b.getPrice() > sim.city.moneyView.get()) {
                        System.out.println(c(RED, "  Can't afford " + list.get(idx).getSimpleName()
                                + ": $" + b.getPrice() + " required, have $"
                                + (int) (double) sim.city.moneyView.get() + "."));
                        break;
                    }
                    synchronized (sim.city.grid) {
                        sim.city.build(b, new Point(y - 1, x - 1));
                    }
                    System.out.println(c(GREEN, "  Built " + list.get(idx).getSimpleName()
                            + " at (" + x + ", " + y + ")"));
                    printMap(sim);
                } catch (NumberFormatException e) {
                    System.out.println(c(RED, "  All arguments must be integers."));
                } catch (UnregisteredBuildingType e) {
                    System.out.println(c(RED, "  Unregistered building type."));
                } catch (Exception e) {
                    System.out.println(c(RED, "  Could not build: " + e.getMessage()));
                }
            }

            case "demolish" -> {
                if (parts.length < 3) {
                    System.out.println(c(RED, "  Usage: demolish <x> <y>   (x=column, y=row)"));
                    break;
                }
                try {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    Point p = new Point(y - 1, x - 1);
                    Buildable existing = sim.city.grid.getBuildingAt(p);
                    if (existing == null) {
                        System.out.println(c(YELLOW, "  No building at (" + x + ", " + y + ")."));
                    } else {
                        int demolishCost = (int) (existing.getPrice() * City.DEMOLISHMENT_COEF);
                        if (demolishCost > sim.city.moneyView.get()) {
                            System.out.println(c(RED, "  Can't afford demolition fee: $" + demolishCost
                                    + " required, have $" + (int) (double) sim.city.moneyView.get() + "."));
                        } else {
                            synchronized (sim.city.grid) {
                                sim.city.demolish(p);
                            }
                            System.out.println(c(GREEN, "  Demolished " + existing.getClass().getSimpleName()
                                    + " at (" + x + ", " + y + ")  (cost: $" + demolishCost + ")"));
                            printMap(sim);
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println(c(RED, "  Coordinates must be integers."));
                }
            }

            case "upgrade" -> {
                if (parts.length < 3) {
                    System.out.println(c(RED, "  Usage: upgrade <x> <y>   (x=column, y=row)"));
                    break;
                }
                try {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    Buildable b = sim.city.grid.getBuildingAt(new Point(y - 1, x - 1));
                    if (b == null) {
                        System.out.println(c(YELLOW, "  No building at (" + x + ", " + y + ")."));
                    } else if (!(b instanceof PublicBuilding pub)) {
                        System.out.println(c(YELLOW, "  Only public buildings can be upgraded."));
                    } else if (pub.getUpgrades().length == 0) {
                        System.out.println(c(YELLOW, "  This building has no upgrades."));
                    } else {
                        System.out.println(c(BOLD, "  Upgrades for " + b.getClass().getSimpleName() + ":"));
                        PublicBuilding.Upgrade[] upgrades = pub.getUpgrades();
                        for (int i = 0; i < upgrades.length; i++) {
                            PublicBuilding.Upgrade u = upgrades[i];
                            String status = u.getIsBuilt()
                                    ? c(GREEN, "built")
                                    : c(YELLOW, "$" + u.getPrice());
                            System.out.printf("    [%d] %-28s %s%n", i, u.getName(), status);
                        }
                        System.out.print("  Enter upgrade index to build (or blank to cancel): ");
                        String ans = sc.nextLine().trim();
                        if (!ans.isEmpty()) {
                            try {
                                int ui = Integer.parseInt(ans);
                                if (ui < 0 || ui >= upgrades.length) {
                                    System.out.println(c(RED, "  Invalid index."));
                                } else if (upgrades[ui].getIsBuilt()) {
                                    System.out.println(c(YELLOW, "  Already built."));
                                } else {
                                    if (sim.city.moneyView.get() < upgrades[ui].getPrice()) {
                                        System.out.println(c(RED, "  Not enough money ($"
                                                + upgrades[ui].getPrice() + " required)."));
                                    } else {
                                        upgrades[ui].build(sim.city);
                                        System.out.println(c(GREEN, "  Upgrade applied!"));
                                    }
                                }
                            } catch (NumberFormatException e) {
                                System.out.println(c(RED, "  Please enter a number."));
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println(c(RED, "  Coordinates must be integers."));
                }
            }

            case "info" -> {
                if (parts.length < 3) {
                    System.out.println(c(RED, "  Usage: info <x> <y>   (x=column, y=row)"));
                    break;
                }
                try {
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    Buildable b = sim.city.grid.getBuildingAt(new Point(y - 1, x - 1));
                    if (b == null) {
                        System.out.println(c(YELLOW, "  No building at (" + x + ", " + y + ")."));
                    } else {
                        System.out.println(c(BOLD, "  " + b.getClass().getSimpleName()
                                + " at (" + x + ", " + y + ")"));
                        for (Map.Entry<String, String> entry : b.getDetailedInfo().entrySet()) {
                            System.out.printf("    %-24s %s%n", entry.getKey() + ":", entry.getValue());
                        }
                        if (!b.getVisitors().isEmpty()) {
                            System.out.println(c(BOLD, "  Visitors (" + b.getVisitors().size() + "):"));
                            for (com.project.city.Citizen citizen : b.getVisitors()) {
                                System.out.println("    " + c(GREY, "---"));
                                citizen.getDetailedInfo()
                                        .forEach((k, v) -> System.out.printf("    %-24s %s%n", k + ":", v));
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println(c(RED, "  Coordinates must be integers."));
                }
                System.out.println();
            }

            case "speed" -> {
                if (parts.length < 2) {
                    System.out.println(c(RED, "  Usage: speed <1-3>  (1=Slow 2=Normal 3=Fast)"));
                    break;
                }
                try {
                    int n = Integer.parseInt(parts[1]);
                    GameSpeed[] speeds = GameSpeed.values();
                    if (n < 1 || n >= speeds.length) {
                        System.out.println(c(RED, "  Speed must be 1–" + (speeds.length - 1)));
                    } else {
                        sim.gameSpeed = speeds[n];
                        System.out.println(c(GREEN, "  Speed set to " + speeds[n]));
                    }
                } catch (NumberFormatException e) {
                    System.out.println(c(RED, "  Speed must be a number 1–3"));
                }
            }

            case "pause" -> {
                sim.gameSpeed = GameSpeed.Stopped;
                System.out.println(c(YELLOW, "  Simulation paused."));
            }

            case "resume" -> {
                sim.gameSpeed = GameSpeed.Normal;
                System.out.println(c(GREEN, "  Simulation resumed at Normal speed."));
            }

            case "loan" -> {
                if (parts.length < 2) {
                    System.out.println(c(RED, "  Usage: loan federal|private"));
                    break;
                }
                if (parts[1].equalsIgnoreCase("federal")) {
                    sim.city.takeOutLoan(new FederalLoan());
                    System.out.println(c(GREEN, "  Federal loan taken out (+$100,000)."));
                } else if (parts[1].equalsIgnoreCase("private")) {
                    sim.city.takeOutLoan(new PrivateLoan());
                    System.out.println(c(GREEN, "  Private loan taken out (+$10,000)."));
                } else {
                    System.out.println(c(RED, "  Unknown loan type. Use: federal or private"));
                }
            }

            case "loans" -> {
                List<Loan> loans = sim.city.loansView.get();
                if (loans.isEmpty()) {
                    System.out.println(c(YELLOW, "  No active loans."));
                } else {
                    System.out.println(c(BOLD, "  Active loans:"));
                    for (int i = 0; i < loans.size(); i++) {
                        Loan l = loans.get(i);
                        // FIX: show whether the player can currently afford to pay each loan
                        boolean canAfford = sim.city.moneyView.get() >= l.paymentLeftView.get();
                        String affordLabel = canAfford ? c(GREEN, "payable") : c(RED, "insufficient funds");
                        System.out.printf("    [%d] %-20s  remaining: $%d  %s%n",
                                i, l.getClass().getSimpleName(), l.paymentLeftView.get(), affordLabel);
                    }
                    System.out.printf("  Total debt: $%.0f%n", sim.totalDebtView.get());
                    System.out.println(c(GREY, "  Last service cost: $"
                            + (int) (double) sim.lastLoansServiceView.get()));
                }
                System.out.println();
            }

            case "payout" -> {
                if (parts.length < 2) {
                    System.out.println(c(RED, "  Usage: payout <loan index>"));
                    break;
                }
                try {
                    int idx = Integer.parseInt(parts[1]);
                    List<Loan> loans = sim.city.loansView.get();
                    if (idx < 0 || idx >= loans.size()) {
                        System.out.println(c(RED, "  Invalid loan index."));
                    } else {
                        Loan loan = loans.get(idx);
                        // FIX: check affordability and give clear feedback, mirroring GUI behaviour.
                        // City.payOutLoan uses < (not <=), so equality is also insufficient.
                        if (sim.city.moneyView.get() <= loan.paymentLeftView.get()) {
                            System.out.println(c(RED, "  Not enough money to pay out this loan. "
                                    + "Need $" + loan.paymentLeftView.get()
                                    + ", have $" + (int) (double) sim.city.moneyView.get() + "."));
                        } else {
                            sim.city.payOutLoan(loan);
                            System.out.println(c(GREEN, "  Loan paid out."));
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println(c(RED, "  Index must be an integer."));
                }
            }

            case "save" -> {
                try {
                    game.createSave();
                    System.out.println(c(GREEN, "  Game saved."));
                } catch (IOException e) {
                    System.out.println(c(RED, "  Save failed: " + e.getMessage()));
                }
            }

            case "menu" -> {
                game.simulator.set(null);
                return false;
            }

            case "help" -> printHelp(true);

            default -> System.out.println(c(RED, "  Unknown command '" + cmd + "'. Type 'help'."));
        }
        return true;
    }

    private static void runSimulationSession(Game game) {
        Simulator sim = game.simulator.get();

        Runnable stopTicks = sim.startSimulationCLI();

        sim.gameSpeed = GameSpeed.Normal;

        printMap(sim);
        printHelp(true);

        Scanner sc = new Scanner(System.in);
        boolean inGame = true;
        while (inGame && game.simulator.get() != null) {
            System.out.print(c(GREY, "[tick:" + sim.currentTickView.get()
                    + "  $" + (int) (double) sim.city.moneyView.get()
                    + "  pop:" + sim.citizensAmountView.get() + "]") + " > ");
            if (!sc.hasNextLine())
                break;
            String line = sc.nextLine().trim();
            if (line.isEmpty())
                continue;
            inGame = handleSimCommand(line, sim, game, sc);
        }
        stopTicks.run();
    }

    private static void mainMenuLoop(Game game, Scanner sc) {
        System.out.println();
        System.out.println(c(BOLD + CYAN,
                "╔══════════════════════════════╗\n" +
                        "║      City Simulator Demo     ║\n" +
                        "╚══════════════════════════════╝"));
        printHelp(false);

        while (true) {
            System.out.print(c(CYAN, "menu") + "> ");
            if (!sc.hasNextLine())
                break;
            String line = sc.nextLine().trim();
            if (line.isEmpty())
                continue;
            String[] parts = line.split("\\s+");
            String cmd = parts[0].toLowerCase();

            switch (cmd) {

                case "new" -> {
                    System.out.print("  City name: ");
                    String name = sc.nextLine().trim();
                    if (name.isEmpty())
                        name = "My New Town";
                    if (name.contains("/")) {
                        System.out.println(c(RED, "  Name cannot contain '/'."));
                        break;
                    }

                    System.out.print("  Map size (10-20): ");
                    int size = 10;
                    try {
                        size = Math.max(10, Math.min(20, Integer.parseInt(sc.nextLine().trim())));
                    } catch (NumberFormatException ignored) {
                    }

                    System.out.println("  Difficulty:");
                    GameDifficulty[] diffs = GameDifficulty.values();
                    for (int i = 0; i < diffs.length; i++)
                        System.out.printf("    [%d] %s%n", i, diffs[i]);
                    System.out.print("  Choose: ");
                    int di = 0;
                    try {
                        di = Math.max(0, Math.min(diffs.length - 1, Integer.parseInt(sc.nextLine().trim())));
                    } catch (NumberFormatException ignored) {
                    }

                    game.simulator.set(null);
                    game.startNewSimulation(size, size, diffs[di], name);
                    runSimulationSession(game);
                    printHelp(false);
                }

                case "load" -> {
                    List<String> saves = game.listSaves();
                    if (saves.isEmpty()) {
                        System.out.println(c(YELLOW, "  No saves found."));
                        break;
                    }
                    System.out.println(c(BOLD, "  Available saves:"));
                    for (int i = 0; i < saves.size(); i++)
                        System.out.printf("    [%d] %s%n", i, saves.get(i));
                    System.out.print("  Choose save index: ");
                    try {
                        int idx = Integer.parseInt(sc.nextLine().trim());
                        if (idx < 0 || idx >= saves.size()) {
                            System.out.println(c(RED, "  Invalid index."));
                            break;
                        }
                        game.simulator.set(null);
                        game.loadSave(saves.get(idx));
                        runSimulationSession(game);
                        printHelp(false);
                    } catch (NumberFormatException e) {
                        System.out.println(c(RED, "  Please enter a number."));
                    } catch (IOException e) {
                        System.out.println(c(RED, "  Failed to load save: " + e.getMessage()));
                    }
                }

                case "credits" ->
                    System.out.println(c(BOLD, "\n  Daniil Poliakov, Lia Bulghadaryan,"
                            + " Mariam Sargsyan, 2026 (C)"));

                case "help" -> printHelp(false);

                case "exit" -> {
                    System.out.println(c(GREY, "  Goodbye!"));
                    System.exit(0);
                }

                default -> System.out.println(c(RED, "  Unknown command '" + cmd + "'. Type 'help'."));
            }
        }
    }

    public static void run() {
        Game game = new Game();
        Scanner sc = new Scanner(System.in);
        mainMenuLoop(game, sc);
    }
}