package net.kyrptonaught.serverutils.mixin.chesttracker;

import net.kyrptonaught.serverutils.chestTracker.ChestTrackerMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChestBlock.class)
public class ChestBlockMixin {

    @Inject(method = "getTicker", at = @At("RETURN"), cancellable = true)
    private void addCustomTicker(World world, BlockState state, BlockEntityType<ChestBlockEntity> type, CallbackInfoReturnable<BlockEntityTicker<ChestBlockEntity>> cir) {
        if (cir.getReturnValue() == null) {
            cir.setReturnValue(ChestTrackerMod::spawnParticleTick);
        }
    }
}
