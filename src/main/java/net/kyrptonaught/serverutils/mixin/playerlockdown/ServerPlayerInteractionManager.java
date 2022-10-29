package net.kyrptonaught.serverutils.mixin.playerlockdown;

import net.kyrptonaught.serverutils.playerlockdown.PlayerLockdownMod;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.server.network.ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManager {

    @Shadow
    @Final
    protected ServerPlayerEntity player;

    @Shadow
    protected ServerWorld world;

    @Shadow
    protected abstract void method_41250(BlockPos pos, boolean success, int sequence, String reason);

    @Inject(method = "processBlockBreakingAction", at = @At("HEAD"), cancellable = true)
    public void preventBlockBreak(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int sequence, CallbackInfo ci) {
        if (PlayerLockdownMod.GLOBAL_LOCKDOWN || PlayerLockdownMod.LOCKEDDOWNPLAYERS.contains(player.getUuidAsString())) {
            this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(pos, this.world.getBlockState(pos)));
            this.method_41250(pos, false, sequence, "block action restricted");
            ci.cancel();
        }
    }

    @Inject(method = "interactBlock", at = @At(value = "HEAD"), cancellable = true)
    public void preventBlockInteract(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        if (PlayerLockdownMod.GLOBAL_LOCKDOWN || PlayerLockdownMod.LOCKEDDOWNPLAYERS.contains(player.getUuidAsString())) {
            cir.setReturnValue(ActionResult.PASS);
            cir.cancel();
        }
    }

    @Inject(method = "interactItem", at = @At(value = "HEAD"), cancellable = true)
    public void preventItemInteract(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (PlayerLockdownMod.GLOBAL_LOCKDOWN || PlayerLockdownMod.LOCKEDDOWNPLAYERS.contains(player.getUuidAsString())) {
            cir.setReturnValue(ActionResult.PASS);
            cir.cancel();
        }
    }
}
