package net.kyrptonaught.serverutils.mixin.tntlighter;

import net.kyrptonaught.serverutils.tntlighter.TNTLighter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.TntEntity;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TntEntity.class)
public abstract class TntEntityMixin extends Entity {

    public TntEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(method = "explode", at = @At("HEAD"), cancellable = true)
    public void nonDestructiveExplosion(CallbackInfo ci) {
        if (TNTLighter.ENABLED) {
            getWorld().createExplosion(this, this.getX(), this.getBodyY(0.0625), this.getZ(), 4.0f, World.ExplosionSourceType.NONE);
            ci.cancel();
        }
    }
}
