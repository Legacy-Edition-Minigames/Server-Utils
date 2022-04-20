package net.kyrptonaught.serverutils.mixin.bigstructures;

import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = StructureBlockBlockEntity.class)
public abstract class StructureBlockEntityMixin {

    @Redirect(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I"))
    private int getMaxSizePos(int value, int min, int max) {
        return MathHelper.clamp(value, -512, 512);
    }
}