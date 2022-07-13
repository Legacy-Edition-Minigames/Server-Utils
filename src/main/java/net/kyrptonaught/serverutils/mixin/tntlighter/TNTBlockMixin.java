package net.kyrptonaught.serverutils.mixin.tntlighter;

import net.kyrptonaught.serverutils.tntlighter.TNTLighter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TntBlock.class)
public abstract class TNTBlockMixin extends Block {

    @Shadow
    private static void primeTnt(World world, BlockPos pos, LivingEntity igniter) {
    }

    public TNTBlockMixin(Settings settings) {
        super(settings);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        if (world != null && placer != null && TNTLighter.ENABLED) {
            primeTnt(world, pos, placer);
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
        }
    }
}
