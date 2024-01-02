package net.kyrptonaught.serverutils.mixin.tntlighter;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.kyrptonaught.serverutils.tntlighter.TNTLighter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Explosion.class)
public class ExplosionMixin {

    @ModifyExpressionValue(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isImmuneToExplosion(Lnet/minecraft/world/explosion/Explosion;)Z"))
    private boolean isAffectedByExplosion(boolean original, @Local Entity entity) {
        if (!TNTLighter.ENABLED)
            return original;

        return entity instanceof ItemEntity;
    }
}
