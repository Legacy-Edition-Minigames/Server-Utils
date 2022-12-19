package net.kyrptonaught.serverutils.critBlocker;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.kyrptonaught.serverutils.Module;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class CritBlockerMod extends Module {

    public static boolean critsBlocked = false;

    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("critblocker")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("enablecrits")
                        .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                    critsBlocked = !enabled;

                                    return 1;
                                }))));
    }
}
