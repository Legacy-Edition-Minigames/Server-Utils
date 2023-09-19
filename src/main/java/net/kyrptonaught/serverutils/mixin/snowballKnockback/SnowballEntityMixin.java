package net.kyrptonaught.serverutils.mixin.snowballKnockback;

import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SnowballEntity.class)
public abstract class SnowballEntityMixin extends ThrownItemEntity {
    public SnowballEntityMixin(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "onEntityHit", at = @At("TAIL"))
    protected void onHitPlayer(EntityHitResult entityHitResult, CallbackInfo ci) {
        Entity entity = entityHitResult.getEntity();
        if (entity instanceof PlayerEntity && !((PlayerEntity) entity).getAbilities().invulnerable) {
            entity.setVelocity(entity.getVelocity().add(this.getVelocity().normalize().multiply(ServerUtilsMod.snowballKnockback.getConfig().snowKnockbackMult)));
            entity.velocityModified = true;
            entity.damage(this.getDamageSources().thrown(this, this.getOwner()), ServerUtilsMod.snowballKnockback.getConfig().snowDamage);
        }
    }
}
