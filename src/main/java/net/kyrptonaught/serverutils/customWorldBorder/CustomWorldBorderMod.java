package net.kyrptonaught.serverutils.customWorldBorder;

import com.mojang.brigadier.CommandDispatcher;
import net.kyrptonaught.serverutils.Module;
import net.kyrptonaught.serverutils.customWorldBorder.duckInterface.CustomWorldBorder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;

public class CustomWorldBorderMod extends Module {

    @Override
    public void onInitialize() {
        CustomWorldBorderManager.tickPlayers();
    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        //libertalia /customWorldBorder -77 0 130 40 52 260
        dispatcher.register(CommandManager.literal("customWorldBorder")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("min", BlockPosArgumentType.blockPos())
                        .then(CommandManager.argument("max", BlockPosArgumentType.blockPos()).executes(context -> {
                            BlockPos min = BlockPosArgumentType.getBlockPos(context, "min");
                            BlockPos max = BlockPosArgumentType.getBlockPos(context, "max");

                            CustomWorldBorderManager.setCustomWorldBorder(context.getSource().getWorld(), min, max);
                            return 1;
                        }))));
    }
}
