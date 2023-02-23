package net.kyrptonaught.serverutils.customWorldBorder;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.kyrptonaught.serverutils.Module;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;

import java.util.HashMap;

public class CustomWorldBorderMod extends Module {

    private static final HashMap<RegistryKey<World>, CustomWorldBorderManager> customWorldBorders = new HashMap<>();

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

    public static void onDimensionUnload(ServerWorld world) {
        customWorldBorders.remove(world.getRegistryKey());
    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        //libertalia /customWorldBorder -77 0 130 40 52 260
        CustomWorldBorderCommand.registerCommands(dispatcher);
    }

    public static CustomWorldBorderManager getCustomWorldBorderManager(ServerWorld world) {
        if (!customWorldBorders.containsKey(world.getRegistryKey()))
            customWorldBorders.put(world.getRegistryKey(), new CustomWorldBorderManager());

        return customWorldBorders.get(world.getRegistryKey());
    }
}
