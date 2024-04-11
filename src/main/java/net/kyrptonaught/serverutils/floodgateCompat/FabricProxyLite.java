package net.kyrptonaught.serverutils.floodgateCompat;

import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
//import one.oktw.VelocityLib;

public class FabricProxyLite {

    public static void hackEarlySendFix() {
        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            //sender.sendPacket(VelocityLib.PLAYER_INFO_CHANNEL, VelocityLib.PLAYER_INFO_PACKET);
        });
    }
}
