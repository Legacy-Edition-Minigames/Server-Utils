package net.kyrptonaught.serverutils.tntlighter;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class TNTLighter {
    public static String MOD_ID = "tntlighter";
    public static boolean ENABLED = false;

    public static void onInitialize() {
        CommandRegistrationCallback.EVENT.register(TNTLighter::registerCommand);
    }

    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(CommandManager.literal("tntlighter")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("enable").then(CommandManager.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> {
                            ENABLED = BoolArgumentType.getBool(context, "enabled");
                            return 1;
                        }))));
    }
}
