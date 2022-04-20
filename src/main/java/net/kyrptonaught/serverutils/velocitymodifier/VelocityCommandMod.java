package net.kyrptonaught.serverutils.velocitymodifier;


import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;


public class VelocityCommandMod {
    public static final String MOD_ID = "velocitycommand";


    public static void onInitialize() {
        CommandRegistrationCallback.EVENT.register(VelocityCommandMod::register);
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean b) {
        FallFlyingCommand.register(dispatcher);
        VelocityCommand.register(dispatcher);
    }
}