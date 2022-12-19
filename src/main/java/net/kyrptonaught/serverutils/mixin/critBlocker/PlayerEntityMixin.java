package net.kyrptonaught.serverutils.mixin.critBlocker;

import net.kyrptonaught.serverutils.critBlocker.CritBlockerMod;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    /*
        @ModifyVariable(method = "attack",
                at = @At(value = "CONSTANT", args = "intValue=1", slice = "bl3", shift = At.Shift.AFTER),
                slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSprinting()Z", ordinal = 1), to = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerEntity;horizontalSpeed:F"), id = "bl3"),
                index = 8)
        public boolean blockCrits(boolean value) {
            System.out.println(value);
            return false;
        }


         */
    @Redirect(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;isSprinting()Z", ordinal = 1))
    public boolean blockCrit(PlayerEntity instance) {
        if (CritBlockerMod.critsBlocked)
            return true;

        return instance.isSprinting();
    }
}
