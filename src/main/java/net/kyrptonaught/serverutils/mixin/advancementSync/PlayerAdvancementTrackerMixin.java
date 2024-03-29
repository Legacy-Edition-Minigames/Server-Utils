package net.kyrptonaught.serverutils.mixin.advancementSync;

import net.kyrptonaught.serverutils.advancementSync.AdvancementSyncMod;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancementTracker.class)
public abstract class PlayerAdvancementTrackerMixin {

    @Shadow
    private ServerPlayerEntity owner;

    @Shadow
    protected abstract void onStatusUpdate(AdvancementEntry advancement);

    @Inject(method = "grantCriterion", at = @At("RETURN"))
    public void syncGrantedAdvancements(AdvancementEntry advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        AdvancementSyncMod.syncGrantedAdvancement(this.owner, advancement, criterionName);
    }

    @Inject(method = "revokeCriterion", at = @At("RETURN"))
    public void syncRevokedAdvancements(AdvancementEntry advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        AdvancementSyncMod.syncRevokedAdvancement(this.owner, advancement, criterionName);
    }

    @Inject(method = "grantCriterion", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/Advancement;rewards()Lnet/minecraft/advancement/AdvancementRewards;"), cancellable = true)
    public void ignoreNullPlayerOwner(AdvancementEntry advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        if (this.owner == null) {
            cir.setReturnValue(true);
            this.onStatusUpdate(advancement);
        }
    }

}
