package net.kyrptonaught.serverutils.scoreboardPlayerInfo;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.serverutils.takeEverything.TakeEverythingMod;
import net.minecraft.util.Identifier;

public class ScoreboardPlayerInfoNetworking {
    private static final Identifier HAS_LEMCLIENTHELPER_PACKET = new Identifier(TakeEverythingMod.MOD_ID, "has_lemclienthelper_packet");
    private static final Identifier HAS_OPTIFINE_PACKET = new Identifier(TakeEverythingMod.MOD_ID, "has_optifine_packet");

    public static void registerReceivePacket() {
        ServerPlayNetworking.registerGlobalReceiver(HAS_LEMCLIENTHELPER_PACKET, (server, player, serverPlayNetworkHandler, packetByteBuf, packetSender) -> {
            server.execute(() -> {
                boolean hasMod = packetByteBuf.readBoolean();
                ScoreboardPlayerInfo.setHasLEMClient(server, player, hasMod);
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(HAS_OPTIFINE_PACKET, (server, player, serverPlayNetworkHandler, packetByteBuf, packetSender) -> {
            server.execute(() -> {
                boolean hasMod = packetByteBuf.readBoolean();
                ScoreboardPlayerInfo.setHasOptifine(server, player, hasMod);
            });
        });
    }
}
