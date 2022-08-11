package net.kyrptonaught.serverutils.mixin.tntlauncher;

import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {
 /*
    @Shadow
    @Final
    private @Nullable Entity entity;

    @Redirect(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/explosion/Explosion;getExposure(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/Entity;)F"))
    private float spoofExposure(Vec3d source, Entity entity) {
        if (TntLauncher.enabled && this.entity instanceof TntEntity)
            return .2f;
        return Explosion.getExposure(source, entity);
    }

    @Redirect(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/ProtectionEnchantment;transformExplosionKnockback(Lnet/minecraft/entity/LivingEntity;D)D"))
    private double spoofExposure(LivingEntity entity, double velocity) {
        if (TntLauncher.enabled && this.entity instanceof TntEntity)
            return velocity;
        return ProtectionEnchantment.transformExplosionKnockback(entity, velocity);
    }

    @Redirect(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;add(DDD)Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d spoofExposure(Vec3d instance, double x, double y, double z) {
        if (TntLauncher.enabled && this.entity instanceof TntEntity)
            return instance.add(x, y, z);
        return instance.add(x, y, z);
    }
*/
}
