package net.kyrptonaught.serverutils.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.kyrptonaught.serverutils.backendLink.discordBridge.Integrations;
import net.kyrptonaught.serverutils.userConfig.AdvancementNoDisplay;
import net.kyrptonaught.serverutils.userConfig.UserConfigStorage;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancementTracker.class)
public class PlayerAdvancementTrackerMixin implements AdvancementNoDisplay {

    @Shadow
    private ServerPlayerEntity owner;
    @Unique
    private boolean forceAdding = false;

    @Override
    public void setForceAdding(boolean display) {
        this.forceAdding = display;
    }

    @WrapOperation(method = "method_53637", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V"))
    private void grantNoDisplay(PlayerManager instance, Text message, boolean overlay, Operation<Void> original) {
        if (!forceAdding) {
            instance.broadcast(message, overlay);
            Integrations.sendAdvancementMessage(message);
        }
    }

    @Inject(method = "grantCriterion", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"))
    private void grant(AdvancementEntry advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        if (!forceAdding) {
            UserConfigStorage.unlockAdvancement(owner, advancement.id(), criterionName);
        }
    }

    @Inject(method = "revokeCriterion", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"))
    private void revoke(AdvancementEntry advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        if (!forceAdding) {
            UserConfigStorage.revokeAdvancement(owner, advancement.id(), criterionName);
        }
    }
}