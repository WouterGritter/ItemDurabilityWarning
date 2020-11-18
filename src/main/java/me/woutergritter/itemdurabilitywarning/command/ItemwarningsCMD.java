package me.woutergritter.itemdurabilitywarning.command;

import me.woutergritter.itemdurabilitywarning.Main;
import me.woutergritter.itemdurabilitywarning.Permissions;
import me.woutergritter.itemdurabilitywarning.command.internal.CommandContext;
import me.woutergritter.itemdurabilitywarning.command.internal.WCommand;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class ItemwarningsCMD extends WCommand {
    public ItemwarningsCMD() {
        super("itemwarnings");
    }

    @Override
    public void execute(CommandContext ctx) {
        ctx.checkPermission(Permissions.ITEMWARNINGS);

        if(ctx.argsLen() == 0) {
            boolean enabled = Main.instance().getItemWarningService().isWarningsEnabled(ctx.checkPlayer());

            ctx.send("status." + (enabled ? "enabled" : "disabled"));
            return;
        }

        if(ctx.argEquals(0, "help")) {
            ctx.send("help");
            return;
        }

        if(ctx.argEquals(0, "ignore")) {
            Player player = ctx.checkPlayer();
            ItemStack hand = player.getInventory().getItemInMainHand();
            if(hand.getType().getMaxDurability() <= 0) {
                ctx.send("ignore.invalid-item-in-hand");
                return;
            }

            boolean ignored = !Main.instance().getItemWarningService().isItemWarningsIgnored(hand);
            Main.instance().getItemWarningService().setItemWarningsIgnored(hand, ignored);

            ctx.send("ignore.successfully-" + (ignored ? "ignored" : "unignored"));
            return;
        }

        if("enable".startsWith(ctx.arg(0).toLowerCase()) ||
                "disable".startsWith(ctx.arg(0).toLowerCase())) {
            Player player = ctx.checkPlayer();
            boolean enabled = "enable".startsWith(ctx.arg(0).toLowerCase());

            if(Main.instance().getItemWarningService().isWarningsEnabled(player) == enabled) {
                ctx.send("set.already-" + (enabled ? "enabled" : "disabled"));
                return;
            }

            Main.instance().getItemWarningService().setWarningsEnabled(player, enabled);

            ctx.send("set.successfully-" + (enabled ? "enabled" : "disabled"));
            return;
        }

        ctx.sendAbsolute("commons.invalid-args", "/itemwarnings help");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        if(!sender.hasPermission(Permissions.ITEMWARNINGS)) {
            return Collections.emptyList();
        }

        if(args.length == 1) {
            return tabCompletePossibilities(args[0],
                    "help", "ignore", "enable", "disable");
        }

        return Collections.emptyList();
    }
}
