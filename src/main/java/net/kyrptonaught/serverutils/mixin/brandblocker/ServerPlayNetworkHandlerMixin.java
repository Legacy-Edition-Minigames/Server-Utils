package net.kyrptonaught.serverutils.mixin.brandblocker;

import net.kyrptonaught.serverutils.VelocityProxyHelper;
import net.kyrptonaught.serverutils.brandBlocker.BrandBlocker;
import net.kyrptonaught.serverutils.brandBlocker.duckInterface.SPNHDelayedJoinBroadcast;
import net.kyrptonaught.serverutils.discordBridge.Integrations;
import net.kyrptonaught.serverutils.scoreboardPlayerInfo.ScoreboardPlayerInfo;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin implements SPNHDelayedJoinBroadcast {

    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(method = "onCustomPayload", at = @At("HEAD"))
    public void onCustomPayload(CustomPayloadC2SPacket packet, CallbackInfo ci) {
        if (packet.getChannel().equals(CustomPayloadC2SPacket.BRAND)) {
            String brand = packet.getData().readString();
            ScoreboardPlayerInfo.checkBrand(player, brand);
            Text msg = BrandBlocker.isBlockedBrand(brand);
            server.execute(() -> {
                if (msg != null) {
                    System.out.println("[BrandBlock] " + player.getEntityName() + " attempted to join with the blocked client brand: " + brand);
                    this.silentLeave = true;
                    VelocityProxyHelper.kickPlayer(player, msg);
                } else {
                    this.server.getPlayerManager().broadcast(storedJoinMSG, false);
                    Integrations.sendJoinMessage(storedJoinMSG);
                }
            });
        }
    }


    @Redirect(method = "onDisconnected", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V"))
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
