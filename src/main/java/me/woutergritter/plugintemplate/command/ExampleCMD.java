package me.woutergritter.plugintemplate.command;

import me.woutergritter.plugintemplate.command.internal.CommandContext;
import me.woutergritter.plugintemplate.command.internal.WCommand;

public class ExampleCMD extends WCommand {
    public ExampleCMD() {
        super("example");
    }

    @Override
    public void execute(CommandContext ctx) {
        ctx.send("output");
    }
}
