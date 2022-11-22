package net.kyrptonaught.serverutils.syncedKeybinds;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.serverutils.ModuleWConfig;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class SyncedKeybinds extends ModuleWConfig<SyncedKeybindsConfig> {

    @Override
    public void onInitialize() {
        SyncedKeybindsNetworking.registerReceivePacket();
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
                SyncedKeybindsNetworking.syncKeybindsToClient(getConfig().keybinds, sender);
        });
    }

    @Override
    public SyncedKeybindsConfig createDefaultConfig() {
        return new SyncedKeybindsConfig();
    }

    public static void keybindPressed(MinecraftServer server, ServerPlayerEntity player, String keyPressed) {
        SyncedKeybindsConfig.KeybindConfigItem keybind = ServerUtilsMod.SyncedKeybindsModule.getConfig().keybinds.get(keyPressed);
        if (keybind != null)
            server.getCommandManager().executeWithPrefix(player.getCommandSource(), keybind.triggerCMD);
    }
}