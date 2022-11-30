package net.kyrptonaught.serverutils.mixin.spectateSqueaker;

import net.kyrptonaught.serverutils.SpectateSqueaker.SpectateSqueakerMod;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {


    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onHandSwing", at = @At("TAIL"))
    public void squeak(HandSwingC2SPacket packet, CallbackInfo ci) {
        if (player.isSpectator() && player.equals(player.getCameraEntity()))
            SpectateSqueakerMod.playerSqueaks(player);
    }
}
