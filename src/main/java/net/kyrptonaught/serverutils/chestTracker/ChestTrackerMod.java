package net.kyrptonaught.serverutils.chestTracker;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.kyrptonaught.serverutils.Module;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.enums.ChestType;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;

public class ChestTrackerMod extends Module {
    public static HashMap<String, HashSet<BlockPos>> playerUsedChests = new HashMap<>();
    public static HashMap<BlockPos, Long> chestsWParticle = new HashMap<>();
    public static boolean enabled = true;
    public static String scoreboardObjective;

    @Override
    public void onInitialize() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (enabled && world.getBlockState(hitResult.getBlockPos()).getBlock() instanceof BlockEntityProvider) {
                if (player instanceof ServerPlayerEntity && ((ServerPlayerEntity) player).interactionManager.getGameMode() != GameMode.SPECTATOR)
                    addChestForPlayer(player, hitResult.getBlockPos());
            }
            return ActionResult.PASS;
        });
    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("chesttracker")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("enabled").then(CommandManager.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> {
                            enabled = BoolArgumentType.getBool(context, "enabled");
                            reset(context.getSource().getServer());
                            return 1;
                        })))
                .then(CommandManager.literal("reset")
                        .executes(context -> {
                            reset(context.getSource().getServer());
                            return 1;
                        }))
                .then(CommandManager.literal("fillChests")
                        .then(CommandManager.argument("chestpos", BlockPosArgumentType.blockPos())
                                .executes(context -> {
                                    BlockPos chestpos = BlockPosArgumentType.getBlockPos(context, "chestpos");
                                    long end = System.currentTimeMillis() + 40000;//40 seconds
                                    chestsWParticle.put(chestpos, end);
                                    chestsWParticle.put(getSecondHalf(context.getSource().getWorld(), chestpos), end);
                                    return 1;
                                })))
                .then(CommandManager.literal("scoreboardObjective").then(CommandManager.argument("scoreboardObjective", StringArgumentType.word())
                        .executes(context -> {
                            scoreboardObjective = StringArgumentType.getString(context, "scoreboardObjective");
                            return 1;
                        }))));
    }

    public static void reset(MinecraftServer server) {
        playerUsedChests.clear();
        chestsWParticle.clear();
        ServerScoreboard scoreboard = server.getScoreboard();
        server.getPlayerManager().getPlayerList().forEach(player -> {
            scoreboard.getPlayerScore(player.getEntityName(), scoreboard.getObjective(scoreboardObjective)).setScore(0);
        });
    }

    public static void addChestForPlayer(PlayerEntity player, BlockPos pos) {
        BlockPos secondHalf = getSecondHalf(player.world, pos);

        chestsWParticle.remove(pos);
        chestsWParticle.remove(secondHalf);

        String uuid = player.getUuidAsString();
        playerUsedChests.computeIfAbsent(uuid, k -> new HashSet<>()).add(pos);
        playerUsedChests.computeIfAbsent(uuid, k -> new HashSet<>()).add(secondHalf);

        ServerScoreboard scoreboard = (ServerScoreboard) player.getScoreboard();
        scoreboard.getPlayerScore(player.getEntityName(), scoreboard.getObjective(scoreboardObjective)).setScore(playerUsedChests.get(uuid).size());
    }

    public static void spawnParticleTick(World world, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (isChestPosValid(pos)) {
            Random random = world.getRandom();
            if (random.nextInt(4) == 0)
                ((ServerWorld) world).spawnParticles(ParticleTypes.WAX_OFF, (double) pos.getX() + random.nextDouble(), pos.getY() + 1 + (random.nextDouble() / 2), (double) pos.getZ() + random.nextDouble(), 0, 0, 4, 0.0, 1);
        }
    }

    public static BlockPos getSecondHalf(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof ChestBlock) {
            if (state.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE)
                return pos.offset(ChestBlock.getFacing(state));
        }
        return pos;
    }

    public static boolean isChestPosValid(BlockPos pos) {
        if (enabled && chestsWParticle.containsKey(pos)) {
            if (System.currentTimeMillis() > chestsWParticle.get(pos)) {
                chestsWParticle.remove(pos);
                return false;
            }
            return true;
        }
        return false;
    }

    public static BlockEntityTicker<BlockEntity> wrapTicker(BlockEntityTicker<BlockEntity> ticker) {
        return (world, pos, state, blockEntity) -> {
            if (ticker != null)
                ticker.tick(world, pos, state, blockEntity);
            spawnParticleTick(world, pos, state, blockEntity);
        };
    }
}