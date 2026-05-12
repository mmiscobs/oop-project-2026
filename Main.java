import farm.cli.FarmCLI;
import farm.ui.FarmGUI;
import com.project.ui.GameInterface;

public class Main {
    public static void main(String[] args) {
        String mode = args.length >= 1 ? args[0].toLowerCase() : "gui";
        int numPlots = args.length >= 2 ? Integer.parseInt(args[1]) : 6;
        int money = args.length >= 3 ? Integer.parseInt(args[2]) : 500;

        switch (mode) {
            case "cli" -> new FarmCLI(numPlots, money).run();
            case "gui" -> javax.swing.SwingUtilities.invokeLater(() ->
                    new FarmGUI(numPlots, money, () -> {
                        try {
                            GameInterface.main(new String[]{});
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })
            );
            default -> {
                System.err.println("Unknown mode '" + mode + "'. Use: gui | cli");
                System.exit(1);
            }
        }
    }
}
