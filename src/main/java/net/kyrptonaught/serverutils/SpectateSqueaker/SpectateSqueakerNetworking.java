package net.kyrptonaught.serverutils.SpectateSqueaker;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.util.Identifier;

public class SpectateSqueakerNetworking {
    private static final Identifier SQUEAK_PACKET = new Identifier(ServerUtilsMod.SpectatorSqueakModule.getMOD_ID(), "squeak_packet");

    public static void registerReceivePacket() {
        ServerPlayNetworking.registerGlobalReceiver(SQUEAK_PACKET, (server, player, serverPlayNetworkHandler, packetByteBuf, packetSender) -> {
            server.execute(() -> {
                SpectateSqueakerMod.playerSqueaks(player);
            });
        });
    }
}
