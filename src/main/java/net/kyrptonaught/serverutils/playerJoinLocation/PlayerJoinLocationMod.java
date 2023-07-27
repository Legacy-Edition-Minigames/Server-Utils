package net.kyrptonaught.serverutils.playerJoinLocation;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.kyrptonaught.serverutils.ModuleWConfig;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class PlayerJoinLocationMod extends ModuleWConfig<PlayerJoinLocationConfig> {
    public static boolean ENABLED = false;

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("playerJoinLocation")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("enable").then(CommandManager.argument("enable", BoolArgumentType.bool())
                        .executes(context -> {
                            ENABLED = BoolArgumentType.getBool(context, "enable");
                            return 1;
                        }))));
    }

    @Override
    public PlayerJoinLocationConfig createDefaultConfig() {
        return new PlayerJoinLocationConfig();
    }
}
