package net.kyrptonaught.serverutils.mixin.switchableresourcepacks;

import net.kyrptonaught.serverutils.switchableresourcepacks.SwitchableResourcepacksMod;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ResourcePackStatusMixin {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onResourcePackStatus", at = @At("TAIL"))
    public void SRPSloadedStatus(ResourcePackStatusC2SPacket resourcePackStatusC2SPacket, CallbackInfo ci) {
        switch (resourcePackStatusC2SPacket.getStatus()) {
            case ACCEPTED -> SwitchableResourcepacksMod.STARTED.grant(this.player);
            case SUCCESSFULLY_LOADED -> SwitchableResourcepacksMod.FINISHED.grant(this.player);
            case FAILED_DOWNLOAD -> SwitchableResourcepacksMod.FAILED.grant(this.player);
        }
    }
}
