package net.kyrptonaught.serverutils.syncedKeybinds;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class SyncedKeybinds {
    public static final String MOD_ID = "syncedkeybinds";

    public static void onInitialize() {
        ServerUtilsMod.configManager.registerFile(MOD_ID, new SyncedKeybindsConfig());
        SyncedKeybindsNetworking.registerReceivePacket();
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (ServerPlayNetworking.canSend(handler.getPlayer(), SyncedKeybindsNetworking.SYNC_KEYBINDS_PACKET))
                SyncedKeybindsNetworking.syncKeybindsToClient(getConfig().keybinds, sender);
        });
    }

    public static SyncedKeybindsConfig getConfig() {
        return (SyncedKeybindsConfig) ServerUtilsMod.configManager.getConfig(MOD_ID);
    }


    public static void keybindPressed(MinecraftServer server, ServerPlayerEntity player, String keyPressed) {
        System.out.println(keyPressed);
        SyncedKeybindsConfig.KeybindConfigItem keybind = getConfig().keybinds.get(keyPressed);
        if (keybind != null)
            server.getCommandManager().executeWithPrefix(player.getCommandSource(), keybind.triggerCMD);
    }
}