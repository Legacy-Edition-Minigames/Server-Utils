package net.kyrptonaught.serverutils.mixin.chesttracker;

import net.kyrptonaught.serverutils.chestTracker.BlockEntityTickerWrapper;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin implements BlockEntityTickerWrapper {

    private BlockEntityTicker<BlockEntity> wrapper;

    @Inject(method = "scheduledTick", at = @At("TAIL"))
    private void addCustomTicker(ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (wrapper != null)
            wrapper.tick(world, pos, world.getBlockState(pos), null);
    }

    @Override
    public void server_Utils$wrap(BlockEntityTicker<BlockEntity> ticker) {
        wrapper = ticker;
    }

    @Override
    public void server_Utils$unWrap() {
        wrapper = null;
    }


}
