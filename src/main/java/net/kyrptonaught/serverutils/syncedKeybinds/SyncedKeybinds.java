package net.kyrptonaught.serverutils.syncedKeybinds;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyrptonaught.serverutils.CMDHelper;
import net.kyrptonaught.serverutils.ModuleWConfig;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

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

    public static void keybindPressed(ServerPlayerEntity player, Identifier keyPressed) {
        SyncedKeybindsConfig.KeybindConfigItem keybind = ServerUtilsMod.SyncedKeybindsModule.getConfig().keybinds.get(keyPressed);
        if (keybind != null)
            CMDHelper.executeAs(player, keybind.triggerCMD);
    }
}