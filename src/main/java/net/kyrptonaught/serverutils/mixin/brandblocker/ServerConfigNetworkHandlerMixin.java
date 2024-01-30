package net.kyrptonaught.serverutils.mixin.brandblocker;

import com.mojang.authlib.GameProfile;
import net.kyrptonaught.serverutils.VelocityProxyHelper;
import net.kyrptonaught.serverutils.brandBlocker.BrandBlocker;
import net.kyrptonaught.serverutils.scoreboardPlayerInfo.ScoreboardPlayerInfo;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.BrandCustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.*;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(ServerConfigurationNetworkHandler.class)
public abstract class ServerConfigNetworkHandlerMixin extends  ServerCommonNetworkHandler {


    @Shadow
    @Final
    private GameProfile profile;

    public ServerConfigNetworkHandlerMixin(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
        super(server, connection, clientData);
    }

    @Override
    public void onCustomPayload(CustomPayloadC2SPacket packet) {
        System.out.println(packet);
        if (packet.payload() instanceof BrandCustomPayload brandPacket) {
            String brand = brandPacket.brand();
            ScoreboardPlayerInfo.getQueuedPlayerData(connection, true).brand = brand;
            Text msg = BrandBlocker.isBlockedBrand(brand);

            if (msg != null) {
                VelocityProxyHelper.kickPlayer(this, this.profile, msg);
            }
        }
    }
}
