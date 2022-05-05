package net.kyrptonaught.serverutils.mixin.waterFreezer;

import net.kyrptonaught.serverutils.waterFreezer.WaterFreezer;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidBlock.class)
public class FluidBlockMixin {

    @Inject(method = "receiveNeighborFluids", at = @At("HEAD"), cancellable = true)
    private void stopFlowing(World world, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (WaterFreezer.getConfig().FROZEN) cir.setReturnValue(true);
    }
}
