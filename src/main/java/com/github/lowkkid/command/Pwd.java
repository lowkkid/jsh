package com.github.lowkkid.command;

import com.github.lowkkid.Main;
import com.github.lowkkid.command.utils.BuiltInCommand;

import java.util.List;

@BuiltInCommand(name = "pwd")
public class Pwd extends Command {

    @Override
    public void executeWithException(List<String> args) {
        stdOut.println(Main.currentDir.toString());

    }
}
