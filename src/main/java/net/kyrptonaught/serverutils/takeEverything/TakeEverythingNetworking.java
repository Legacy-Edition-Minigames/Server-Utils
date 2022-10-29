package net.kyrptonaught.serverutils.takeEverything;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.util.Identifier;

public class TakeEverythingNetworking {
    private static final Identifier TAKE_EVERYTHING_PACKET = new Identifier(ServerUtilsMod.TakeEverythingModule.getMOD_ID(), "take_everything_packet");

    public static void registerReceivePacket() {
        ServerPlayNetworking.registerGlobalReceiver(TAKE_EVERYTHING_PACKET, (server, player, serverPlayNetworkHandler, packetByteBuf, packetSender) -> {
            server.execute(() -> {
                TakeEverythingHelper.takeEverything(player);
            });
        });
    }
}
