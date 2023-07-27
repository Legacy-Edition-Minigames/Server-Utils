package net.kyrptonaught.serverutils.floodgateCompat;

import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyrptonaught.serverutils.Module;
import net.kyrptonaught.serverutils.scoreboardPlayerInfo.ScoreboardPlayerInfo;
import one.oktw.VelocityLib;
import org.geysermc.floodgate.api.FloodgateApi;

public class FloodgateCompatMod extends Module {

    @Override
    public void onInitialize() {
        //temp hack to fix compat with Fabric Proxy lite/floodgate/LuckPerms
        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            sender.sendPacket(VelocityLib.PLAYER_INFO_CHANNEL, VelocityLib.PLAYER_INFO_PACKET);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ScoreboardPlayerInfo.setBedrockClient(handler.player, FloodgateApi.getInstance().isFloodgatePlayer(handler.player.getUuid()));
        });
    }
}
