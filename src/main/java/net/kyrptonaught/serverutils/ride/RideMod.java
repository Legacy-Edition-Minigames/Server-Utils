package net.kyrptonaught.serverutils.ride;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.kyrptonaught.serverutils.chestTracker.ChestTrackerMod;
import net.minecraft.block.ChestBlock;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public class RideMod {
    public static void onInitialize() {

        CommandRegistrationCallback.EVENT.register(RideMod::registerCommand);
    }

    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher, boolean b) {
        dispatcher.register(CommandManager.literal("ride")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("entity", EntityArgumentType.entity())
                        .executes(context -> {
                            Entity entity = EntityArgumentType.getEntity(context, "entity");
                            PlayerEntity player = context.getSource().getPlayer();
                            if (entity != player)
                                player.startRiding(entity);
                            else context.getSource().sendFeedback(new LiteralText("You cannot ride yourself"), false);
                            return 1;
                        })));
    }
}