package net.kyrptonaught.serverutils.velocitymodifier;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class VelocityCommandMod {
    public static final String MOD_ID = "velocitycommand";

    public static void onInitialize() {
        CommandRegistrationCallback.EVENT.register(VelocityCommandMod::registerCommand);
    }

    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        FallFlyingCommand.register(dispatcher);
        VelocityCommand.register(dispatcher);
    }
}