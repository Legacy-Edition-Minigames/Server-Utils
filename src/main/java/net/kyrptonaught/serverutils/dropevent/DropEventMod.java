package net.kyrptonaught.serverutils.dropevent;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.kyrptonaught.serverutils.ModuleWConfig;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class DropEventMod extends ModuleWConfig<DropEventConfig> {
    public static boolean ENABLED = true;

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("dropevent")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("enabled").then(CommandManager.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> {
                            ENABLED = BoolArgumentType.getBool(context, "enabled");
                            return 1;
                        })))
                .then(CommandManager.literal("command").then(CommandManager.argument("command", StringArgumentType.greedyString())
                        .executes(context -> {
                            getConfig().runCommand = StringArgumentType.getString(context, "command");
                            saveConfig();
                            return 1;
                        }))));
    }

    @Override
    public DropEventConfig createDefaultConfig() {
        return new DropEventConfig();
    }
}
