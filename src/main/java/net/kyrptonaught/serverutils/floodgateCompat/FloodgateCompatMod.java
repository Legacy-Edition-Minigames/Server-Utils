package net.kyrptonaught.serverutils.floodgateCompat;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.kyrptonaught.serverutils.Module;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class FloodgateCompatMod extends Module {
    private static final Identifier PLAYER_INFO_CHANNEL = new Identifier("velocity", "player_info");
    private static final PacketByteBuf PLAYER_INFO_PACKET = new PacketByteBuf(Unpooled.wrappedBuffer(new byte[]{4}).asReadOnly());

    @Override
    public void onInitialize() {
        //temp hack to fix compat with Fabric Proxy lite/floodgate/LuckPerms
        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            sender.sendPacket(PLAYER_INFO_CHANNEL, PLAYER_INFO_PACKET);
        });
    }
}
