package net.kyrptonaught.serverutils.advancementSync;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyrptonaught.serverutils.ModuleWConfig;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.backendServer.BackendServerModule;
import net.kyrptonaught.serverutils.personatus.PersonatusProfile;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.PathUtil;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;

import java.nio.file.Files;
import java.nio.file.Path;

public class AdvancementSyncMod extends ModuleWConfig<AdvancementSyncConfig> {
    public static String MOD_ID = "advancementsync";

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (getConfig().syncOnJoin && !BackendServerModule.backendRunning()) {
                BackendServerModule.asyncGet(getUrl("getAdvancements", handler.player), (success, response) -> {
                    if (success)
                        server.execute(() -> {
                            try {
                                Path path = server.getSavePath(WorldSavePath.ADVANCEMENTS).resolve(handler.player.getUuidAsString() + ".json");
                                PathUtil.createDirectories(path.getParent());
                                Files.writeString(path, response.body());
                                handler.player.getAdvancementTracker().reload(server.getAdvancementLoader());
                            } catch (Exception e) {
                                System.out.println("Failed to sync advancements from backend");
                                e.printStackTrace();
                            }
                        });
                });
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> BackendServerModule.asyncPost(AdvancementSyncMod.getUrl("unloadPlayer", handler.player)));
    }

    @Override
    public AdvancementSyncConfig createDefaultConfig() {
        return new AdvancementSyncConfig();
    }

    public static void syncGrantedAdvancement(ServerPlayerEntity serverPlayerEntity, AdvancementEntry advancement, String criterionName) {
        if (!BackendServerModule.backendRunning()) {
            JsonObject advancementJson = Util.getResult(Advancement.CODEC.encodeStart(JsonOps.INSTANCE, advancement.value()), IllegalStateException::new).getAsJsonObject();
            JsonObject json = new JsonObject();
            json.addProperty("advancementID", advancement.id().toString());
            json.add("advancement", advancementJson);
            json.addProperty("criterionName", criterionName);
            BackendServerModule.asyncPost(getUrl("addAdvancements", serverPlayerEntity), ServerUtilsMod.getGson().toJson(json));
        }
    }

    public static void syncRevokedAdvancement(ServerPlayerEntity serverPlayerEntity, AdvancementEntry advancement, String criterionName) {
        if (!BackendServerModule.backendRunning()) {
            JsonObject advancementJson = Util.getResult(Advancement.CODEC.encodeStart(JsonOps.INSTANCE, advancement.value()), IllegalStateException::new).getAsJsonObject();
            JsonObject json = new JsonObject();
            json.addProperty("advancementID", advancement.id().toString());
            json.add("advancement", advancementJson);
            json.addProperty("criterionName", criterionName);
            BackendServerModule.asyncPost(getUrl("removeAdvancements", serverPlayerEntity), ServerUtilsMod.getGson().toJson(json));
        }
    }

    public static String getUrl(String route, ServerPlayerEntity player) {
        return route + "/" + ((PersonatusProfile) player.getGameProfile()).getRealProfile().getId().toString();
    }
}