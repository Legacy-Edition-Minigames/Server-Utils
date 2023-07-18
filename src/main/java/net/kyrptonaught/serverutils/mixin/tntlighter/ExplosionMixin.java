package net.kyrptonaught.serverutils.mixin.tntlighter;

import net.kyrptonaught.serverutils.tntlighter.TNTLighter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Set;

@Mixin(Explosion.class)
public class ExplosionMixin {

    @Inject(method = "collectBlocksAndDamageEntities", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"), locals = LocalCapture.CAPTURE_FAILHARD)
    public void dontEffectItemEntities(CallbackInfo ci, Set<BlockPos> set, int i, float q, int k, int l, int r, int s, int t, int u, List<Entity> list) {
        if (TNTLighter.ENABLED)
            list.removeIf(entity -> entity instanceof ItemEntity);
    }
}
