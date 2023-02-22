package net.kyrptonaught.serverutils.customWorldBorder;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.kyrptonaught.serverutils.Module;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.border.WorldBorder;

public class CustomWorldBorderMod extends Module {

    @Override
    public void onInitialize() {
        ServerTickEvents.START_WORLD_TICK.register(world -> {
            CustomWorldBorderManager worldBorderManager = getCustomWorldBorderManager(world);
            if (!worldBorderManager.enabled) return;

            WorldBorder mainBorder = world.getWorldBorder();
            for (ServerPlayerEntity player : world.getPlayers()) {
                worldBorderManager.tickPlayer(player, mainBorder);
            }
        });

        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            getCustomWorldBorderManager(origin).playerBorders.remove(player.getUuid());
            getCustomWorldBorderManager(destination).playerBorders.remove(player.getUuid());
        });
    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        //libertalia /customWorldBorder -77 0 130 40 52 260
        CustomWorldBorderCommand.registerCommands(dispatcher);
    }

    public static CustomWorldBorderManager getCustomWorldBorderManager(ServerWorld world) {
        PersistentStateManager stateMan = world.getPersistentStateManager();
        String id = ServerUtilsMod.CustomWorldBorder.getMOD_ID();
        return ((CustomWorldBorderStorage) stateMan.getOrCreate((nbt) -> CustomWorldBorderStorage.fromNbt(world, nbt), CustomWorldBorderStorage::new, id)).getCustomWorldBorderManager();
    }
}
