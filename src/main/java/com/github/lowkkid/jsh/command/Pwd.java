package com.github.lowkkid.jsh.command;

import com.github.lowkkid.jsh.Main;
import java.util.List;

public class Pwd extends Command {

    @Override
    public void executeWithException(List<String> args) {
        stdOut.println(Main.currentDir.toString());

    }
}
