package net.kyrptonaught.serverutils.mixin.smallinv;

import net.kyrptonaught.serverutils.smallInv.SmallInvMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {

    @Shadow
    public abstract ItemStack getStack();

    @Inject(method = "canInsert", at = @At("HEAD"), cancellable = true)
    public void canInsert(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (SmallInvMod.ENABLED && SmallInvMod.isSmallSlot(getStack()))
            cir.setReturnValue(false);
    }

    @Inject(method = "canTakeItems", at = @At("HEAD"), cancellable = true)
    public void canTakeItems(PlayerEntity playerEntity, CallbackInfoReturnable<Boolean> cir) {
        if (SmallInvMod.ENABLED && SmallInvMod.isSmallSlot(getStack()))
            cir.setReturnValue(false);
    }
}
