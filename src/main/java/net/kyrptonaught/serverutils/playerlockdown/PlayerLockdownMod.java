package net.kyrptonaught.serverutils.playerlockdown;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.kyrptonaught.serverutils.Module;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
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
                        .executes(context -> executeLockdown(null, BoolArgumentType.getBool(context, "enabled"))))

                .then(CommandManager.argument("players", EntityArgumentType.players())
                        .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> executeLockdown(EntityArgumentType.getPlayers(context, "players"), BoolArgumentType.getBool(context, "enabled")))))

                .then(CommandManager.literal("clear")
                        .executes(context -> {
                            GLOBAL_LOCKDOWN = false;
                            LOCKEDDOWNPLAYERS.clear();
                            return 1;
                        })));
        dispatcher.register(CommandManager.literal("playerfreeze")
                .requires((source) -> source.hasPermissionLevel(2))

                .then(CommandManager.argument("players", EntityArgumentType.players())
                        .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> executeFreeze(EntityArgumentType.getPlayers(context, "players"), BoolArgumentType.getBool(context, "enabled")))))

                .then(CommandManager.literal("clear")
                        .executes(context -> {
                            executeFreeze(context.getSource().getServer().getPlayerManager().getPlayerList(), false);
                            return 1;
                        })));
    }

    private static int executeLockdown(Collection<ServerPlayerEntity> players, boolean enabled) {
        if (players == null) {
            GLOBAL_LOCKDOWN = enabled;
            return 1;
        }
        for (ServerPlayerEntity serverPlayerEntity : players) {
            if (enabled) {
                LOCKEDDOWNPLAYERS.add(serverPlayerEntity.getUuidAsString());
            } else {
                LOCKEDDOWNPLAYERS.remove(serverPlayerEntity.getUuidAsString());
            }
        }

        return 1;
    }

    private static int executeFreeze(Collection<ServerPlayerEntity> players, boolean enabled) {
        for (ServerPlayerEntity serverPlayerEntity : players) {
            if (enabled) {
                serverPlayerEntity.getAbilities().setFlySpeed(0);
                serverPlayerEntity.getAbilities().setWalkSpeed(0);
                serverPlayerEntity.getAbilities().flying = false;
                serverPlayerEntity.sendAbilitiesUpdate();
                serverPlayerEntity.startFallFlying();
                serverPlayerEntity.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(serverPlayerEntity.getId(), new StatusEffectInstance(StatusEffects.JUMP_BOOST, -1, 250, false, false)));

            } else {
                serverPlayerEntity.getAbilities().setFlySpeed(.05f);
                serverPlayerEntity.getAbilities().setWalkSpeed(.1f);

                StatusEffectInstance effect = serverPlayerEntity.getStatusEffect(StatusEffects.JUMP_BOOST);
                if (effect != null) {
                    serverPlayerEntity.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(serverPlayerEntity.getId(), effect));
                } else {
                    serverPlayerEntity.networkHandler.sendPacket(new RemoveEntityStatusEffectS2CPacket(serverPlayerEntity.getId(), StatusEffects.JUMP_BOOST));
                }
            }
        }

        return 1;
    }
}