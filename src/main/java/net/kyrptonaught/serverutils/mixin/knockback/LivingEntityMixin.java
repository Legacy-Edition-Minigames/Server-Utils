package net.kyrptonaught.serverutils.mixin.knockback;

import net.kyrptonaught.serverutils.knockback.KnockbackMod;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

//Written by CrumbledHam
@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Redirect(method = "takeKnockback", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isOnGround()Z"))
    public boolean forceKnockbackAsIfOnGround(LivingEntity livingentity) {
        if (KnockbackMod.ENABLED)
            return true;

        return livingentity.isOnGround();
    }
}