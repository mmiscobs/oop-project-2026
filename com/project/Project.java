package com.project;

import com.project.cli.CLIInterface;
import com.project.ui.GameInterface;

public class Project {
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("-cli"))
            CLIInterface.run();
        else
            GameInterface.run();
    }
}
