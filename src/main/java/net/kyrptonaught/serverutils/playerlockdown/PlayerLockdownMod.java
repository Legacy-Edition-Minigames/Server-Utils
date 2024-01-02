package net.kyrptonaught.serverutils.playerlockdown;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.kyrptonaught.serverutils.Module;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.Collections;
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
        for (ServerPlayerEntity player : players) {
            if (enabled) {
                LOCKEDDOWNPLAYERS.add(player.getUuidAsString());
            } else {
                LOCKEDDOWNPLAYERS.remove(player.getUuidAsString());
            }
        }

        return 1;
    }

    private static int executeFreeze(Collection<ServerPlayerEntity> players, boolean enabled) {
        for (ServerPlayerEntity player : players) {
            if (enabled) {

                PlayerAbilities abilities = new PlayerAbilities();
                abilities.setWalkSpeed(0);
                abilities.setFlySpeed(0);
                abilities.invulnerable = true;
                abilities.allowModifyWorld = false;
                player.networkHandler.sendPacket(new PlayerAbilitiesS2CPacket(abilities));

                EntityAttributeInstance walk_speed = new EntityAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED, entityAttributeInstance -> {
                });
                walk_speed.setBaseValue(0);
                player.networkHandler.sendPacket(new EntityAttributesS2CPacket(player.getId(), Collections.singleton(walk_speed)));

                player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(player.getId(), new StatusEffectInstance(StatusEffects.JUMP_BOOST, -1, 250, false, false)));

                player.networkHandler.sendPacket(new HealthUpdateS2CPacket(player.getHealth(), 1, player.getHungerManager().getSaturationLevel()));
            } else {
                player.sendAbilitiesUpdate();

                player.networkHandler.sendPacket(new EntityAttributesS2CPacket(player.getId(), Collections.singleton(player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED))));

                StatusEffectInstance effect = player.getStatusEffect(StatusEffects.JUMP_BOOST);
                if (effect != null)
                    player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(player.getId(), effect));
                else
                    player.networkHandler.sendPacket(new RemoveEntityStatusEffectS2CPacket(player.getId(), StatusEffects.JUMP_BOOST));

                player.networkHandler.sendPacket(new HealthUpdateS2CPacket(player.getHealth(), player.getHungerManager().getFoodLevel(), player.getHungerManager().getSaturationLevel()));
            }
        }

        return 1;
    }
}