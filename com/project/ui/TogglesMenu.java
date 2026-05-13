package com.project.ui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.JButton;

public class TogglesMenu extends JPanel {
    public interface Action {
        String getName();

        String getStopName();

        Runnable enable(Runnable onEnd);
    }

    public TogglesMenu(List<Action> actions) {
        super(new GridLayout(0, 1, 10, 10));
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        ArrayList<JButton> buttons = new ArrayList<>();
        for (Action action : actions) {
            JButton button = new JButton(action.getName());
            button.setPreferredSize(new Dimension(550, 0));

            Runnable rerender = () -> this.revalidate();
            button.addActionListener(new ActionListener() {
                private boolean clicked = false;
                private Runnable stopAction = null;

                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (clicked) {
                        stopAction.run();
                        clicked = false;

                        return;
                    }
                    clicked = true;
                    for (JButton otherButton : buttons) {
                        if (otherButton == button)
                            continue;
                        otherButton.setEnabled(false);
                    }
                    button.setText(action.getStopName());
                    rerender.run();
                    Runnable revertButtons = () -> {
                        clicked = false;
                        button.setText(action.getName());
                        for (JButton otherButton : buttons) {
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
            buttons.add(button);
            this.add(button);
        }
    }
}
