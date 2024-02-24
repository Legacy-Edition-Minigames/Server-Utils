package net.kyrptonaught.serverutils.syncedKeybinds;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;

import static net.kyrptonaught.serverutils.ServerUtilsMod.SyncedKeybindsModule;

public class SyncedKeybindsNetworking {
    public static final Identifier SYNC_KEYBINDS_PACKET = new Identifier(SyncedKeybindsModule.getMOD_ID(), "sync_keybinds_packet");
    public static final Identifier KEYBIND_PRESSED_PACKET = new Identifier(SyncedKeybindsModule.getMOD_ID(), "keybind_pressed_packet");

    public static void registerReceivePacket() {
        ServerPlayNetworking.registerGlobalReceiver(KEYBIND_PRESSED_PACKET, (server, player, serverPlayNetworkHandler, packetByteBuf, packetSender) -> {
            Identifier keybindId = packetByteBuf.readIdentifier();

            server.execute(() -> {
                SyncedKeybinds.keybindPressed(player, keybindId);
            });
        });
    }

    public static void syncKeybindsToClient(HashMap<Identifier, SyncedKeybindsConfig.KeybindConfigItem> keybinds, PacketSender packetSender) {
        PacketByteBuf packetByteBuf = new PacketByteBuf(Unpooled.buffer());
        packetByteBuf.writeInt(keybinds.size());
        keybinds.forEach((s, keybindConfigItem) -> keybindConfigItem.writeToPacket(s, packetByteBuf));
        packetSender.sendPacket(SYNC_KEYBINDS_PACKET, packetByteBuf);
    }
}
