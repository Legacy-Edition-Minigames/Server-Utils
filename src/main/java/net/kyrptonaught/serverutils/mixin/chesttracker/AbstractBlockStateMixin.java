package net.kyrptonaught.serverutils.mixin.chesttracker;

import net.kyrptonaught.serverutils.chestTracker.ChestTrackerMod;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class AbstractBlockStateMixin {

    @Inject(method = "getBlockEntityTicker", at = @At("RETURN"), cancellable = true)
    private <T extends BlockEntity> void addCustomTicker(World world, BlockEntityType<T> blockEntityType, CallbackInfoReturnable<@Nullable BlockEntityTicker<BlockEntity>> cir) {
        if (!world.isClient)
            cir.setReturnValue(ChestTrackerMod.wrapTicker(cir.getReturnValue()));
    }
}
