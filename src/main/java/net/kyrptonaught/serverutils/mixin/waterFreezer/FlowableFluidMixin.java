package net.kyrptonaught.serverutils.mixin.waterFreezer;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.kyrptonaught.serverutils.ServerUtilsMod.WaterFreezerModule;

@Mixin(FlowableFluid.class)
public class FlowableFluidMixin {

    @Inject(method = "tryFlow", at = @At("HEAD"), cancellable = true)
    private void stopFlowing(WorldAccess world, BlockPos fluidPos, FluidState state, CallbackInfo ci) {
        if (WaterFreezerModule.getConfig().FROZEN) ci.cancel();
    }

    @Inject(method = "flow", at = @At("HEAD"), cancellable = true)
    private void stopFlowing(WorldAccess world, BlockPos pos, BlockState state, Direction direction, FluidState fluidState, CallbackInfo ci) {
        if (WaterFreezerModule.getConfig().FROZEN) ci.cancel();
    }
}
