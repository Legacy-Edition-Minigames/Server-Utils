package net.kyrptonaught.serverutils.mixin.smallinv;

import net.kyrptonaught.serverutils.smallInv.SmallInvMod;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.PlayerScreenHandler;
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
        if (SmallInvMod.ENABLED &&
                !(player.currentScreenHandler instanceof PlayerScreenHandler) &&
                packet.getSlot() > -1 &&
                SmallInvMod.isSmallSlot(this.player.currentScreenHandler.getSlot(packet.getSlot()).getStack())) {
            if (packet.getActionType() == SlotActionType.PICKUP) { // clicked
                SmallInvMod.executeClicked(player);
            } else if (packet.getActionType() == SlotActionType.SWAP && packet.getButton() == 40) { //offhand pressed
                int slot = PlayerScreenHandler.OFFHAND_ID;
                this.player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(player.playerScreenHandler.syncId, player.playerScreenHandler.nextRevision(), slot, player.playerScreenHandler.getSlot(slot).getStack()));
            }
        }

    }
}
