package me.woutergritter.itemdurabilitywarning.command;

import me.woutergritter.itemdurabilitywarning.command.internal.CommandContext;
import me.woutergritter.itemdurabilitywarning.command.internal.WCommand;

public class ExampleCMD extends WCommand {
    public ExampleCMD() {
        super("example");
    }

    @Override
    public void execute(CommandContext ctx) {
        ctx.send("output");
    }
}
