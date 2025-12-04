package com.github.lowkkid.command;

import java.util.Arrays;

public class Echo implements Command {

    @Override
    public void execute(String[] args) {
        Arrays.stream(args).forEach(arg -> System.out.print(arg + " "));
        System.out.println();
    }
}
