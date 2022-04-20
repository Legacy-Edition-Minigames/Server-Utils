package net.kyrptonaught.serverutils.takeEverything;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class TakeEverythingNetworking {
    private static final Identifier TAKE_EVERYTHING_PACKET = new Identifier(TakeEverythingMod.MOD_ID, "take_everything_packet");

    public static void registerReceivePacket() {
        ServerPlayNetworking.registerGlobalReceiver(TAKE_EVERYTHING_PACKET, (server, player, serverPlayNetworkHandler, packetByteBuf, packetSender) -> {
            server.execute(() -> {
                TakeEverythingHelper.takeEverything(player);
            });
        });
    }
}
