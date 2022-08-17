package net.kyrptonaught.serverutils.mixin.brandblocker;

import net.kyrptonaught.serverutils.brandBlocker.BrandBlocker;
import net.kyrptonaught.serverutils.brandBlocker.duckInterface.SPNHDelayedJoinBroadcast;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin implements SPNHDelayedJoinBroadcast {

    @Shadow
    public ServerPlayerEntity player;

    @Shadow
    public abstract ClientConnection getConnection();

    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(method = "onCustomPayload", at = @At("HEAD"))
    public void onCustomPayload(CustomPayloadC2SPacket packet, CallbackInfo ci) {
        if (packet.getChannel().equals(CustomPayloadC2SPacket.BRAND)) {
            String brand = packet.getData().readString();
            Text msg = BrandBlocker.isBlockedBrand(brand);
            server.execute(() -> {
                if (msg != null) {
                    System.out.println("[BrandBlock] " + player.getEntityName() + " attempted to join with the blocked client brand: " + brand);
                    this.silentLeave = true;
                    if (brand.contains("(Velocity)"))
                        BrandBlocker.kickVelocity(player, getConnection(), msg);
                    else
                        BrandBlocker.kickMC(player, getConnection(), msg);
                } else {
                    this.server.getPlayerManager().broadcast(storedJoinMSG, MessageType.SYSTEM, Util.NIL_UUID);
                }
            });
        }
    }


    @Redirect(method = "onDisconnected", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V"))
    public void silentLeave(PlayerManager instance, Text message, MessageType type, UUID sender) {
        if (!silentLeave) instance.broadcast(message, type, sender);
    }

    private boolean silentLeave = false;
    private Text storedJoinMSG = new LiteralText("ERROR");

    @Override
    public void storeJoinMSG(Text joinMSG) {
        this.storedJoinMSG = joinMSG;
    }
}
