package net.kyrptonaught.serverutils.mixin.advancementSync;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyrptonaught.serverutils.advancementSync.AdvancementSyncMod;
import net.minecraft.SharedConstants;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancementTracker.class)
public abstract class PlayerAdvancementTrackerMixin {

    @Shadow
    public abstract AdvancementProgress getProgress(Advancement advancement);

    @Shadow
    @Final
    private static Gson GSON;

    @Shadow
    private ServerPlayerEntity owner;

    @Inject(method = "grantCriterion", at = @At("RETURN"))
    public void syncGrantedAdvancements(Advancement advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        AdvancementProgress advancementProgress = this.getProgress(advancement);
        AdvancementSyncMod.syncGrantedAdvancement(this.owner, serializeToJson(advancement.getId(), advancementProgress));
    }

    @Inject(method = "revokeCriterion", at = @At("RETURN"))
    public void syncRevokedAdvancements(Advancement advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        AdvancementProgress advancementProgress = this.getProgress(advancement);

        System.out.println("revoked: " + advancement.getId() + " " + criterionName);
        if (advancementProgress.isAnyObtained())
            AdvancementSyncMod.syncGrantedAdvancement(this.owner, serializeToJson(advancement.getId(), advancementProgress));
        else{

            //AdvancementSyncMod.syncRevokedAdvancement(this.owner, object);
        }
    }

    private static String serializeToJson(Identifier id, AdvancementProgress advancementProgress) {
        JsonObject object = new JsonObject();
        JsonElement element = GSON.toJsonTree(advancementProgress);
        object.add(id.toString(), element);
        object.addProperty("DataVersion", SharedConstants.getGameVersion().getWorldVersion());
        return GSON.toJson(object);
    }
}
