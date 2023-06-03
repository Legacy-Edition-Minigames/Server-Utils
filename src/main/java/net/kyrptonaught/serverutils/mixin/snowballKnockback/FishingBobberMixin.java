package net.kyrptonaught.serverutils.mixin.snowballKnockback;

import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberMixin extends ProjectileEntity {
    @Shadow
    private Entity hookedEntity;

    private FishingBobberMixin(EntityType<? extends FishingBobberMixin> entityType, World world) {
        super(entityType, world);
    }

    @ModifyArg(method = "pullHookedEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;multiply(D)Lnet/minecraft/util/math/Vec3d;"))
    public double changePullMultiplier(double multiplier) {
        return ServerUtilsMod.snowballKnockback.getConfig().fishingRodPullMult;
    }

    @Inject(method = "pullHookedEntity", at = @At("TAIL"))
    public void updateVelocity(CallbackInfo ci) {
        this.hookedEntity.velocityModified = true;
    }
}
