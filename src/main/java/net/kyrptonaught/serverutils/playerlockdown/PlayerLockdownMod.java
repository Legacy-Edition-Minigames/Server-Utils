package net.kyrptonaught.serverutils.playerlockdown;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.kyrptonaught.serverutils.Module;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.HashSet;

public class PlayerLockdownMod extends Module {
    public static boolean GLOBAL_LOCKDOWN = false;

    public static final HashSet<String> LOCKEDDOWNPLAYERS = new HashSet<>();

    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("lockdown")
                .requires((source) -> source.hasPermissionLevel(2))

                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> execute(null, BoolArgumentType.getBool(context, "enabled"))))

                .then(CommandManager.argument("players", EntityArgumentType.players())
                        .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> execute(EntityArgumentType.getPlayers(context, "players"), BoolArgumentType.getBool(context, "enabled")))))

                .then(CommandManager.literal("clear")
                        .executes(context -> {
                            GLOBAL_LOCKDOWN = false;
                            LOCKEDDOWNPLAYERS.clear();
                            return 1;
                        })));
    }

    private static int execute(Collection<ServerPlayerEntity> players, boolean enabled) {
        if (players == null) {
            GLOBAL_LOCKDOWN = enabled;
            return 1;
        }
        players.forEach(serverPlayerEntity -> {
            if (enabled) LOCKEDDOWNPLAYERS.add(serverPlayerEntity.getUuidAsString());
            else LOCKEDDOWNPLAYERS.remove(serverPlayerEntity.getUuidAsString());
        });

        return 1;
    }
}