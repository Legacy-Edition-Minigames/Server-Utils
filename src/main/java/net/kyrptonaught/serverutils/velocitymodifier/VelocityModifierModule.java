package net.kyrptonaught.serverutils.velocitymodifier;

import com.mojang.brigadier.CommandDispatcher;
import net.kyrptonaught.serverutils.Module;
import net.minecraft.server.command.ServerCommandSource;

public class VelocityModifierModule extends Module {

    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        FallFlyingCommand.register(dispatcher);
        VelocityCommand.register(dispatcher);
    }
}