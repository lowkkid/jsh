package com.github.lowkkid.command;

import com.github.lowkkid.utils.FileUtils;
import com.github.lowkkid.Main;

import java.util.Arrays;

public class Type implements Command {

    @Override
    public void execute(String[] args) {
        Arrays.stream(args).forEach((arg) -> {
            if (Main.isBultInCommand(arg)) {
                System.out.println(arg + " is a shell builtin");
            } else {
                String cmdDir = FileUtils.existsInPathDirectories(arg);
                if (cmdDir != null) {
                    System.out.println(arg + " is " + cmdDir);
                } else {
                    System.out.println(arg + ": not found");
                }
            }
        });
    }


}
