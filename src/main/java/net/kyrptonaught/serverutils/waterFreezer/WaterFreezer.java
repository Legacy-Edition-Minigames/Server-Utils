package net.kyrptonaught.serverutils.waterFreezer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.kyrptonaught.serverutils.ModuleWConfig;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class WaterFreezer extends ModuleWConfig<WaterFreezerConfig> {

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("waterFreezer")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("freeze").then(CommandManager.argument("freeze", BoolArgumentType.bool())
                        .executes(context -> {
                            getConfig().FROZEN = BoolArgumentType.getBool(context, "freeze");
                            saveConfig();
                            return 1;
                        }))));
    }

    @Override
    public WaterFreezerConfig createDefaultConfig() {
        return new WaterFreezerConfig();
    }
}