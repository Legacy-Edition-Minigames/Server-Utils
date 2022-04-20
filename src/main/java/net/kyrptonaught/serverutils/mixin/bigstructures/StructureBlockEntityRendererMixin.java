package net.kyrptonaught.serverutils.mixin.bigstructures;

import net.minecraft.client.render.block.entity.StructureBlockBlockEntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructureBlockBlockEntityRenderer.class)
public class StructureBlockEntityRendererMixin {

    @Inject(method = "getRenderDistance", at = @At("RETURN"), cancellable = true)
    public void extendRenderRange(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(Integer.MAX_VALUE);
    }
}
