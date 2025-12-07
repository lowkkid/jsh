package com.github.lowkkid.command;

import java.util.List;

public class Echo implements Command {

    @Override
    public void execute(List<String> args) {
        args.forEach(arg -> System.out.print(arg + " "));
        System.out.println();
    }
}
