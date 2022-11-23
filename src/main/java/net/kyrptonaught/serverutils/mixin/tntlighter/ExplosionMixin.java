package net.kyrptonaught.serverutils.mixin.tntlighter;

import net.kyrptonaught.serverutils.tntlighter.TNTLighter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Explosion.class)
public class ExplosionMixin {

    @Redirect(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"))
    public boolean dontDamageItems(Entity instance, DamageSource source, float amount) {
        if (TNTLighter.ENABLED && instance instanceof ItemEntity)
            return false;
        return instance.damage(source, amount);
    }

    @Redirect(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"))
    public void dontKnockbackEntities(Entity instance, Vec3d velocity) {
        if (TNTLighter.ENABLED && instance instanceof ItemEntity)
            return;
        instance.setVelocity(velocity);
    }
}
