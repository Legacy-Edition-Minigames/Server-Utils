package net.kyrptonaught.serverutils.mixin.cpslimiter;

import net.kyrptonaught.serverutils.cpslimiter.CPSLimiter;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = {"net.minecraft.server.network.ServerPlayNetworkHandler$1"})
public class ServerPlayNetworkHandlerMixin {

    @Shadow
    @Final
    ServerPlayNetworkHandler field_28963;

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;attack(Lnet/minecraft/entity/Entity;)V"), cancellable = true)
    public void limitCPS(CallbackInfo ci) {
        if (!CPSLimiter.isValidCPS(this.field_28963.player))
            ci.cancel();
    }

}
