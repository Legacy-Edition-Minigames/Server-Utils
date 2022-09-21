package net.kyrptonaught.serverutils.mixin.advancementSync;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.kyrptonaught.serverutils.advancementSync.AdvancementSyncMod;
import net.kyrptonaught.serverutils.advancementSync.PATLoadFromString;
import net.minecraft.SharedConstants;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.CriterionProgress;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Mixin(PlayerAdvancementTracker.class)
public abstract class PlayerAdvancementTrackerMixin implements PATLoadFromString {

    private static final Gson GSON2 = new GsonBuilder().registerTypeAdapter(AdvancementProgress.class, new AdvancementProgress.Serializer()).registerTypeAdapter(Identifier.class, new Identifier.Serializer()).create();

    @Shadow
    public abstract AdvancementProgress getProgress(Advancement advancement);

    @Shadow
    private ServerPlayerEntity owner;

    @Shadow
    @Final
    private static TypeToken<Map<Identifier, AdvancementProgress>> JSON_TYPE;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    protected abstract void initProgress(Advancement advancement, AdvancementProgress progress);

    @Shadow
    protected abstract void rewardEmptyAdvancements(ServerAdvancementLoader advancementLoader);

    @Shadow
    protected abstract void updateCompleted();

    @Shadow
    protected abstract void beginTrackingAllAdvancements(ServerAdvancementLoader advancementLoader);

    @Shadow
    public abstract void clearCriteria();

    @Shadow
    @Final
    private Map<Advancement, AdvancementProgress> advancementToProgress;

    @Shadow
    @Final
    private Set<Advancement> visibleAdvancements;

    @Shadow
    @Final
    private Set<Advancement> visibilityUpdates;

    @Shadow
    @Final
    private Set<Advancement> progressUpdates;

    @Shadow
    private boolean dirty;

    @Shadow
    private @Nullable Advancement currentDisplayTab;

    @Inject(method = "grantCriterion", at = @At("RETURN"))
    public void syncGrantedAdvancements(Advancement advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            AdvancementProgress advancementProgress = this.getProgress(advancement);
            JsonObject advancementJson = new JsonObject();
            JsonObject criterionJson = new JsonObject();
            CriterionProgress progress = advancementProgress.getCriterionProgress(criterionName);
            if (progress != null && progress.isObtained())
                criterionJson.add(criterionName, progress.toJson());
            advancementJson.add("criteria", criterionJson);
            if (advancementProgress.isDone())
                advancementJson.addProperty("done", true);

            AdvancementSyncMod.syncGrantedAdvancement(this.owner, serializeToJson(advancement.getId(), advancementJson));
        }
    }

    @Inject(method = "revokeCriterion", at = @At("RETURN"))
    public void syncRevokedAdvancements(Advancement advancement, String criterionName, CallbackInfoReturnable<Boolean> cir) {
        if(cir.getReturnValue()) {
            JsonObject object = new JsonObject();
            object.addProperty("advancement", advancement.getId().toString());
            object.addProperty("criteria", criterionName);
            object.addProperty("DataVersion", SharedConstants.getGameVersion().getWorldVersion());
            AdvancementSyncMod.syncRevokedAdvancement(this.owner, GSON2.toJson(object));
        }
    }

    private static String serializeToJson(Identifier id, JsonObject advancementJson) {
        JsonObject object = new JsonObject();
        object.add(id.toString(), advancementJson);
        object.addProperty("DataVersion", SharedConstants.getGameVersion().getWorldVersion());
        return GSON2.toJson(object);
    }

    @Override
    public void loadFromString(ServerAdvancementLoader advancementLoader, String json) {
        this.clearCriteria();
        this.advancementToProgress.clear();
        this.visibleAdvancements.clear();
        this.visibilityUpdates.clear();
        this.progressUpdates.clear();
        this.dirty = true;
        this.currentDisplayTab = null;
        Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, GSON2.fromJson(json, JsonObject.class));
        //if (!dynamic.get("DataVersion").asNumber().result().isPresent()) {
        //    dynamic = dynamic.set("DataVersion", dynamic.createInt(1343));
        //}
        //dynamic = this.dataFixer.update(DataFixTypes.ADVANCEMENTS.getTypeReference(), dynamic, dynamic.get("DataVersion").asInt(0), SharedConstants.getGameVersion().getWorldVersion());
        dynamic = dynamic.remove("DataVersion");

        Map<Identifier, AdvancementProgress> map = GSON2.getAdapter(JSON_TYPE).fromJsonTree(dynamic.getValue());
        if (map == null) {
            throw new JsonParseException("Found null for advancements");
        }

        Stream<Map.Entry<Identifier, AdvancementProgress>> stream = map.entrySet().stream().sorted(Map.Entry.comparingByValue());
        for (Map.Entry<Identifier, AdvancementProgress> entry : stream.toList()) {
            Advancement advancement = advancementLoader.get(entry.getKey());
            if (advancement == null) {
                LOGGER.warn("Ignored synced advancement '{}' for {} - it doesn't exist anymore?", entry.getKey(), owner.getDisplayName());
                continue;
            }
            this.initProgress(advancement, entry.getValue());
        }
        this.rewardEmptyAdvancements(advancementLoader);
        this.updateCompleted();
        this.beginTrackingAllAdvancements(advancementLoader);
    }
}
