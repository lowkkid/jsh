package com.github.lowkkid.jsh.command;

import com.github.lowkkid.jsh.Main;
import com.github.lowkkid.jsh.command.utils.BuiltInCommand;

import java.util.List;

@BuiltInCommand(name = "pwd")
public class Pwd extends Command {

    @Override
    public void executeWithException(List<String> args) {
        stdOut.println(Main.currentDir.toString());

    }
}
