package com.github.lowkkid.command;

import com.github.lowkkid.Main;

import java.util.List;

public class Pwd implements Command {


    @Override
    public void execute(List<String> args) {
        System.out.println(Main.currentDir.toString());

    }
}
