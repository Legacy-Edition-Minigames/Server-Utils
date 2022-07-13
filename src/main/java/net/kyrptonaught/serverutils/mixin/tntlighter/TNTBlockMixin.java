package net.kyrptonaught.serverutils.mixin.tntlighter;

import net.kyrptonaught.serverutils.tntlighter.TNTLighter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @Inject(method = "onDestroyedByExplosion", at = @At("HEAD"), cancellable = true)
    public void preventReignite(World world, BlockPos pos, Explosion explosion, CallbackInfo ci) {
        if (world != null && explosion.getCausingEntity() instanceof PlayerEntity && TNTLighter.ENABLED) {
            ci.cancel();
        }
    }
}
