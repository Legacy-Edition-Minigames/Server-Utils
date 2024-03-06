package net.kyrptonaught.serverutils.velocityserverswitch;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.kyrptonaught.serverutils.Module;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class VelocityServerSwitchMod extends Module {

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("velocityserverswitch")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("servername", StringArgumentType.word())
                        .executes((commandContext) -> {
                            String servername = StringArgumentType.getString(commandContext, "servername");
                            //todo re-add with 1.20.5
                            //VelocityProxyHelper.switchServer(commandContext.getSource().getPlayer(), servername);
                            return 1;
                        })));
    }
}
