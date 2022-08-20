package net.kyrptonaught.serverutils.scoreboardPlayerInfo;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ScoreboardPlayerInfoNetworking {
    private static final Identifier HAS_MODS_PACKET = new Identifier(ScoreboardPlayerInfo.MOD_ID, "has_mods_packet");

    public static void registerReceivePacket() {
        ServerPlayNetworking.registerGlobalReceiver(HAS_MODS_PACKET, (server, player, serverPlayNetworkHandler, packetByteBuf, packetSender) -> {
            Boolean hasLCH = getNextBool(packetByteBuf);
            Boolean hasOptishit = getNextBool(packetByteBuf);
            Boolean hasController = getNextBool(packetByteBuf);

            server.execute(() -> {
                if (hasLCH != null) ScoreboardPlayerInfo.setHasLEMClient(server, player, hasLCH);
                if (hasOptishit != null) ScoreboardPlayerInfo.setHasOptifine(server, player, hasOptishit);
                if (hasController != null) ScoreboardPlayerInfo.setHasControllerMod(server, player, hasController);
            });
        });
    }

    public static Boolean getNextBool(PacketByteBuf packetByteBuf) {
        if (packetByteBuf.isReadable(1))
            return packetByteBuf.readBoolean();
        return null;
    }
}
