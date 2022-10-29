package net.kyrptonaught.serverutils.tntlighter;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.kyrptonaught.serverutils.Module;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class TNTLighter extends Module {
    public static boolean ENABLED = false;

    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("tntlighter")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("enable").then(CommandManager.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> {
                            ENABLED = BoolArgumentType.getBool(context, "enabled");
                            return 1;
                        }))));
    }
}
