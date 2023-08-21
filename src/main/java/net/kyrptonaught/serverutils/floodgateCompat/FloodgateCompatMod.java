package net.kyrptonaught.serverutils.floodgateCompat;

import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.kyrptonaught.serverutils.Module;
import one.oktw.VelocityLib;

public class FloodgateCompatMod extends Module {

    @Override
    public void onInitialize() {
        //temp hack to fix compat with Fabric Proxy lite/floodgate/LuckPerms
        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            sender.sendPacket(VelocityLib.PLAYER_INFO_CHANNEL, VelocityLib.PLAYER_INFO_PACKET);
        });
    }
}
