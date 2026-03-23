package net.kyrptonaught.serverutils.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.kyrptonaught.serverutils.backendLink.discordBridge.Integrations;
import net.kyrptonaught.serverutils.userConfig.AdvancementNoDisplay;
import net.kyrptonaught.serverutils.userConfig.UserConfigStorage;
import net.minecraft.advancement.*;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

    @Inject(method = "sendUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", shift = At.Shift.BEFORE))
    private void hideToast(ServerPlayerEntity player, CallbackInfo ci, @Local Map<Identifier, AdvancementProgress> map) {
        if (forceAdding) {
            for (Identifier identifier : map.keySet()) {
                Advancement advancement = player.server.getAdvancementLoader().get(identifier).value();

                if (advancement.display().isPresent() && advancement.display().get().shouldShowToast()) {
                    AdvancementDisplay display = advancement.display().get();
                    AdvancementDisplay display2 = new AdvancementDisplay(display.getIcon(), display.getTitle(), display.getDescription(), display.getBackground(), display.getFrame(), false, display.shouldAnnounceToChat(), display.isHidden());
                    Advancement advancement2 = new Advancement(advancement.parent(), Optional.of(display2), advancement.rewards(), advancement.criteria(), advancement.requirements(), advancement.sendsTelemetryEvent(), advancement.name());

                    player.networkHandler.sendPacket(new AdvancementUpdateS2CPacket(false, Set.of(new AdvancementEntry(identifier, advancement2)), Set.of(), new HashMap<>()));
                }
            }
        }
    }

    @Inject(method = "sendUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", shift = At.Shift.AFTER))
    private void hideToast2(ServerPlayerEntity player, CallbackInfo ci, @Local Map<Identifier, AdvancementProgress> map) {
        if (forceAdding) {
            for (Identifier identifier : map.keySet()) {
                Advancement advancement = player.server.getAdvancementLoader().get(identifier).value();

                if (advancement.display().isPresent() && advancement.display().get().shouldShowToast()) {
                    AdvancementDisplay display = advancement.display().get();
                    AdvancementDisplay display2 = new AdvancementDisplay(display.getIcon(), display.getTitle(), display.getDescription(), display.getBackground(), display.getFrame(), true, display.shouldAnnounceToChat(), display.isHidden());
                    Advancement advancement2 = new Advancement(advancement.parent(), Optional.of(display2), advancement.rewards(), advancement.criteria(), advancement.requirements(), advancement.sendsTelemetryEvent(), advancement.name());

                    player.networkHandler.sendPacket(new AdvancementUpdateS2CPacket(false, Set.of(new AdvancementEntry(identifier, advancement2)), Set.of(), new HashMap<>()));
                }
            }
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