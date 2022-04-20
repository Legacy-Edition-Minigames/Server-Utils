package net.kyrptonaught.serverutils.velocitymodifier;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.Collections;

public class FallFlyingCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("startfallfly")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("player", EntityArgumentType.players())
                        .executes(context -> execute(EntityArgumentType.getPlayers(context, "player"))))
                .executes(context -> execute(Collections.singleton(context.getSource().getPlayer()))));
    }

    private static int execute(Collection<ServerPlayerEntity> players) {
        players.forEach(player -> {
            player.getAbilities().flying = false;
            player.sendAbilitiesUpdate();
            player.startFallFlying();
        });
        return 1;
    }
}