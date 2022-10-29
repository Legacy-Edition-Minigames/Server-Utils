package net.kyrptonaught.serverutils.mixin.waterFreezer;

import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.kyrptonaught.serverutils.ServerUtilsMod.WaterFreezerModule;

@Mixin(FluidBlock.class)
public class FluidBlockMixin {

    @Inject(method = "receiveNeighborFluids", at = @At("HEAD"), cancellable = true)
    private void stopFlowing(World world, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if (WaterFreezerModule.getConfig().FROZEN) cir.setReturnValue(true);
    }
}
