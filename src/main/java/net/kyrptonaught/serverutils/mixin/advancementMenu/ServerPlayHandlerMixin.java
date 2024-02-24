package net.kyrptonaught.serverutils.mixin.advancementMenu;

import net.kyrptonaught.serverutils.advancementMenu.AdvancementMenuMod;
import net.minecraft.network.packet.c2s.play.AdvancementTabC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayHandlerMixin {

    @Shadow
    public abstract ServerPlayerEntity getPlayer();

    @Inject(method = "onAdvancementTab", at = @At("HEAD"), cancellable = true)
    public void runCustomCMD(AdvancementTabC2SPacket packet, CallbackInfo ci) {
        if (packet.getAction() == AdvancementTabC2SPacket.Action.OPENED_TAB && AdvancementMenuMod.EXECUTE_COMMAND != null) {
            AdvancementMenuMod.executeCommand(getPlayer());
            ci.cancel();
        }
    }
}
