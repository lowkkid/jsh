package com.github.lowkkid.command;

import com.github.lowkkid.Main;

public class Pwd implements Command {


    @Override
    public void execute(String[] args) {
        System.out.println(Main.currentDir.toString());

    }
}
