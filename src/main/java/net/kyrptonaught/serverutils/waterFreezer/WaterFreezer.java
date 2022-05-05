package net.kyrptonaught.serverutils.waterFreezer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class WaterFreezer {
    public static String MOD_ID = "waterfreezer";

    public static void onInitialize() {
        ServerUtilsMod.configManager.registerFile(MOD_ID, new WaterFreezerConfig());
        CommandRegistrationCallback.EVENT.register(WaterFreezer::registerCommand);
    }

    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher, boolean b) {
        dispatcher.register(CommandManager.literal("waterFreezer")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("freeze").then(CommandManager.argument("freeze", BoolArgumentType.bool())
                        .executes(context -> {
                            getConfig().FROZEN = BoolArgumentType.getBool(context, "freeze");
                            ServerUtilsMod.configManager.save(MOD_ID);
                            return 1;
                        }))));
    }

    public static WaterFreezerConfig getConfig() {
        return (WaterFreezerConfig) ServerUtilsMod.configManager.getConfig(MOD_ID);
    }
}
