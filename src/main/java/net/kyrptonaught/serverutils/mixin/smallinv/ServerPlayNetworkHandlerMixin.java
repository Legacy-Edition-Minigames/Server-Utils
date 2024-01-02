package net.kyrptonaught.serverutils.mixin.smallinv;

import net.kyrptonaught.serverutils.smallInv.SmallInvMod;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
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

    @Inject(method = "onClickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandler;onSlotClick(IILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V"))
    public void clientClicked(ClickSlotC2SPacket packet, CallbackInfo ci) {
        if (SmallInvMod.ENABLED && packet.getActionType() == SlotActionType.PICKUP && !(player.currentScreenHandler instanceof PlayerScreenHandler)) {
            System.out.println(packet.getSlot() + " " + packet.getButton() + " " + packet.getActionType() + " " + packet.getStack());
            Slot slot = this.player.currentScreenHandler.getSlot(packet.getSlot());
            if (SmallInvMod.isSmallSlot(slot.getStack())) {
                SmallInvMod.executeClicked(player);
            }
        }
    }
}
