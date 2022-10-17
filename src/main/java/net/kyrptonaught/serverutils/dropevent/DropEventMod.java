package net.kyrptonaught.serverutils.dropevent;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class DropEventMod {

    public static final String MOD_ID = "dropevent";
    public static boolean ENABLED = true;

    public static void onInitialize() {
        ServerUtilsMod.configManager.registerFile(MOD_ID, new DropEventConfig());
        CommandRegistrationCallback.EVENT.register(DropEventMod::registerCommand);
    }

    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
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
                            ServerUtilsMod.configManager.save(MOD_ID);
                            return 1;
                        }))));
    }

    public static DropEventConfig getConfig() {
        return (DropEventConfig) ServerUtilsMod.configManager.getConfig(MOD_ID);
    }
}
