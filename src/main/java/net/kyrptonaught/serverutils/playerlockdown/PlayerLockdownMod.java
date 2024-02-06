package net.kyrptonaught.serverutils.playerlockdown;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyrptonaught.serverutils.Module;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.EntityAttributesS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class PlayerLockdownMod extends Module {
    public static boolean GLOBAL_LOCKDOWN = false;

    public static final HashSet<String> LOCKEDDOWNPLAYERS = new HashSet<>();
    public static final HashMap<String, Vec3d> FROZENPLAYERS = new HashMap<>();

    @Override
    public void onInitialize() {
        super.onInitialize();
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            player.getAbilities().setWalkSpeed(0.1f);
            player.getAbilities().setFlySpeed(0.05f);
            player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1f);
            FROZENPLAYERS.remove(player.getUuidAsString());
        });

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if (!FROZENPLAYERS.isEmpty()) {
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    Vec3d pos = FROZENPLAYERS.get(player.getUuidAsString());
                    if (pos != null && !pos.equals(player.getPos())) {
                        player.teleport(pos.getX(), pos.getY(), pos.getZ());
                    }
                }
            }
        });
    }

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
                        .then(CommandManager.literal("true")
                                .executes(context -> {
                                    return executeFreeze(EntityArgumentType.getPlayers(context, "players"), null, true);
                                })
                                .then(CommandManager.argument("pos", Vec3ArgumentType.vec3())
                                        .executes(context -> {
                                            return executeFreeze(EntityArgumentType.getPlayers(context, "players"), Vec3ArgumentType.getVec3(context, "pos"), true);
                                        })))
                        .then(CommandManager.literal("false")
                                .executes(context -> {
                                    return executeFreeze(EntityArgumentType.getPlayers(context, "players"), null, false);
                                })))
                .then(CommandManager.literal("clear")
                        .executes(context -> {
                            executeFreeze(context.getSource().getServer().getPlayerManager().getPlayerList(), null, false);
                            FROZENPLAYERS.clear();
                            return 1;
                        })));
    }

    public static int executeLockdown(Collection<ServerPlayerEntity> players, boolean enabled) {
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

    public static int executeFreeze(Collection<ServerPlayerEntity> players, Vec3d pos, boolean enabled) {
        for (ServerPlayerEntity player : players) {
            if (enabled) {
                player.getAbilities().setWalkSpeed(0);
                player.getAbilities().setFlySpeed(0);
                player.sendAbilitiesUpdate();

                player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
                player.networkHandler.sendPacket(new EntityAttributesS2CPacket(player.getId(), Collections.singleton(player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED))));

                player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(player.getId(), new StatusEffectInstance(StatusEffects.JUMP_BOOST, -1, 250, false, false)));

                player.networkHandler.sendPacket(new HealthUpdateS2CPacket(player.getHealth(), 1, player.getHungerManager().getSaturationLevel()));

                if (pos == null) pos = player.getPos();
                FROZENPLAYERS.put(player.getUuidAsString(), pos);
            } else {
                player.getAbilities().setWalkSpeed(0.1f);
                player.getAbilities().setFlySpeed(0.05f);
                player.sendAbilitiesUpdate();

                player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.1f);
                player.networkHandler.sendPacket(new EntityAttributesS2CPacket(player.getId(), Collections.singleton(player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED))));

                StatusEffectInstance effect = player.getStatusEffect(StatusEffects.JUMP_BOOST);
                if (effect != null)
                    player.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(player.getId(), effect));
                else
                    player.networkHandler.sendPacket(new RemoveEntityStatusEffectS2CPacket(player.getId(), StatusEffects.JUMP_BOOST));

                player.networkHandler.sendPacket(new HealthUpdateS2CPacket(player.getHealth(), player.getHungerManager().getFoodLevel(), player.getHungerManager().getSaturationLevel()));
                FROZENPLAYERS.remove(player.getUuidAsString());
            }
        }

        return 1;
    }
}