package net.kyrptonaught.serverutils.mixin.brandblocker;

import net.kyrptonaught.serverutils.brandBlocker.BrandBlocker;
import net.kyrptonaught.serverutils.scoreboardPlayerInfo.ScoreboardPlayerInfo;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.BrandCustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(ServerConfigurationNetworkHandler.class)
public abstract class ServerConfigNetworkHandlerMixin extends ServerCommonNetworkHandler {

    public ServerConfigNetworkHandlerMixin(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
        super(server, connection, clientData);
    }

    @Override
    public void onCustomPayload(CustomPayloadC2SPacket packet) {
        if (packet.payload() instanceof BrandCustomPayload brandPacket) {
            String brand = brandPacket.brand();
            ScoreboardPlayerInfo.getQueuedPlayerData(connection, true).brand = brand;
            Text msg = BrandBlocker.isBlockedBrand(brand);

            if (msg != null) {
                this.connection.disconnect(msg);
            }
        }
    }
}
