package net.kyrptonaught.serverutils.mixin.brandblocker;

import net.kyrptonaught.serverutils.VelocityProxyHelper;
import net.kyrptonaught.serverutils.brandBlocker.BrandBlocker;
import net.kyrptonaught.serverutils.brandBlocker.duckInterface.SPNHDelayedJoinBroadcast;
import net.kyrptonaught.serverutils.discordBridge.Integrations;
import net.kyrptonaught.serverutils.scoreboardPlayerInfo.ScoreboardPlayerInfo;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.BrandCustomPayload;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin extends ServerCommonNetworkHandler implements SPNHDelayedJoinBroadcast {

    @Shadow
    public ServerPlayerEntity player;

    public ServerPlayNetworkHandlerMixin(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
        super(server, connection, clientData);
    }

    @Override
    public void onCustomPayload(CustomPayloadC2SPacket packet) {
        super.onCustomPayload(packet);
        if (packet.payload() instanceof BrandCustomPayload brandPacket) {
            String brand = brandPacket.brand();
            ScoreboardPlayerInfo.checkBrand(player, brand);
            Text msg = BrandBlocker.isBlockedBrand(brand);
            server.execute(() -> {
                if (msg != null) {
                    System.out.println("[BrandBlock] " + player.getNameForScoreboard() + " attempted to join with the blocked client brand: " + brand);
                    this.silentLeave = true;
                    VelocityProxyHelper.kickPlayer(player, msg);
                } else {
                    this.server.getPlayerManager().broadcast(storedJoinMSG, false);
                    Integrations.sendJoinMessage(player, storedJoinMSG);
                }
            });
        }
    }


    @Redirect(method = "cleanUp", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V"))
    public void silentLeave(PlayerManager instance, Text message, boolean overlay) {
        if (!silentLeave) {
            instance.broadcast(message, overlay);
            Integrations.sendLeaveMessage(message);
        }
    }

    private boolean silentLeave = false;
    private Text storedJoinMSG = Text.literal("ERROR");

    @Override
    public void storeJoinMSG(Text joinMSG) {
        this.storedJoinMSG = joinMSG;
    }
}
