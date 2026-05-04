package ui;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class TogglesMenu extends JMenu {
    public interface Action {
        String getName();

        String getStopName();

        Runnable enable(Runnable onEnd);
    }

    public TogglesMenu(String name, List<Action> actions) {
        super(name);
        ArrayList<JMenuItem> buttons = new ArrayList<>();
        for (Action action : actions) {
            JMenuItem button = new JMenuItem(action.getName());

            button.addActionListener(new ActionListener() {
                private boolean clicked = false;
                private Runnable stopAction = null;

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (clicked) {
                        stopAction.run();

                        return;
                    }
                    clicked = true;
                    for (JMenuItem otherButton : buttons) {
                        if (otherButton == button)
                            continue;
                        otherButton.setEnabled(false);
                    }
                    button.setText(action.getStopName());
                    Runnable revertButtons = () -> {
                        clicked = false;
                        button.setText(action.getName());
                        for (JMenuItem otherButton : buttons) {
                            if (otherButton == button)
                                continue;
                            otherButton.setEnabled(true);
                        }
                    };
                    Runnable cancelAction = action.enable(revertButtons);
                    stopAction = () -> {
                        cancelAction.run();
                        revertButtons.run();
                    };
                };
            });
            this.add(button);
        }
    }
}
