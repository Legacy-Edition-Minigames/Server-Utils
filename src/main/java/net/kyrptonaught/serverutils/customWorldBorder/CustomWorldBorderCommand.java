package net.kyrptonaught.serverutils.customWorldBorder;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class CustomWorldBorderCommand {

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("customWorldBorder")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("dimension", DimensionArgumentType.dimension())
                        .then(CommandManager.literal("set")
                                .then(CommandManager.argument("min", BlockPosArgumentType.blockPos())
                                        .then(CommandManager.argument("max", BlockPosArgumentType.blockPos()).executes(context -> {
                                            ServerWorld world = DimensionArgumentType.getDimensionArgument(context, "dimension");
                                            BlockPos min = BlockPosArgumentType.getBlockPos(context, "min");
                                            BlockPos max = BlockPosArgumentType.getBlockPos(context, "max");

                                            CustomWorldBorderMod.getCustomWorldBorderManager(world).setCustomWorldBorder(world, min, max);
                                            return 1;
                                        })))
                                .then(CommandManager.argument("entities", EntityArgumentType.entities()).executes(context -> {
                                    ServerWorld world = DimensionArgumentType.getDimensionArgument(context, "dimension");
                                    Entity[] entities = EntityArgumentType.getEntities(context, "entities").toArray(Entity[]::new);
                                    if (entities.length >= 2) {
                                        CustomWorldBorderMod.getCustomWorldBorderManager(world).setCustomWorldBorder(world, entities[0].getBlockPos(), entities[1].getBlockPos());
                                    }
                                    return 1;
                                }))
                                .then(CommandManager.argument("entity1", EntityArgumentType.entity())
                                        .then(CommandManager.argument("entity2", EntityArgumentType.entity()).executes(context -> {
                                            ServerWorld world = DimensionArgumentType.getDimensionArgument(context, "dimension");
                                            Entity entity1 = EntityArgumentType.getEntity(context, "entity1");
                                            Entity entity2 = EntityArgumentType.getEntity(context, "entity2");

                                            CustomWorldBorderMod.getCustomWorldBorderManager(world).setCustomWorldBorder(world, entity1.getBlockPos(), entity2.getBlockPos());
                                            return 1;
                                        }))))
                        .then(CommandManager.literal("enable")
                                .then(CommandManager.argument("enabled", BoolArgumentType.bool()).executes(context -> {
                                    ServerWorld world = DimensionArgumentType.getDimensionArgument(context, "dimension");
                                    boolean enabled = BoolArgumentType.getBool(context, "enabled");

                                    CustomWorldBorderMod.getCustomWorldBorderManager(world).setEnabled(world, enabled);
                                    return 1;
                                })))
                ));
    }
}