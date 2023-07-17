package net.kyrptonaught.serverutils.mixin.takeeverything;

import net.kyrptonaught.serverutils.takeEverything.TakeEverythingHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {
    @Shadow
    private ItemStack cursorStack;

    @Shadow
    @Final
    public DefaultedList<Slot> slots;


    @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
    public void takeEverything(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (!player.getWorld().isClient) {
            if (actionType.equals(SlotActionType.QUICK_MOVE)) {
                Slot slot = slots.get(slotIndex);
                if (slot.canTakeItems(player) && !(slot.inventory instanceof PlayerInventory) && !slot.getStack().isEmpty() && TakeEverythingHelper.isSwappableItem(slot.getStack())) {
                    ItemStack oldStack = TakeEverythingHelper.equipOrSwapArmor(player, slot.getStack(), false); //return already equippedStack or empty
                    if (!oldStack.isEmpty()) slot.setStack(oldStack);
                }
            } else if ((button == 0 && actionType.equals(SlotActionType.PICKUP_ALL)) ||
                    (button == 2 && actionType.equals(SlotActionType.CLONE)))
                if (cursorStack.isEmpty())
                    if (slotIndex >= 0 && slotIndex < this.slots.size() && this.slots.get(slotIndex).getStack().isEmpty()) {
                        TakeEverythingHelper.takeEverything((ServerPlayerEntity) player);
                        ci.cancel();
                    }
        }
    }
}