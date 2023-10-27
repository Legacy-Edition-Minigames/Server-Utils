package net.kyrptonaught.serverutils.advancementSync;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyrptonaught.serverutils.ModuleWConfig;
import net.kyrptonaught.serverutils.backendServer.BackendServerModule;
import net.kyrptonaught.serverutils.personatus.PersonatusProfile;
import net.minecraft.server.network.ServerPlayerEntity;

public class AdvancementSyncMod extends ModuleWConfig<AdvancementSyncConfig> {
    public static String MOD_ID = "advancementsync";

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (getConfig().syncOnJoin) {
                try {
                    BackendServerModule.asyncGet(getUrl("getAdvancements", handler.player), (success, resposne) -> {
                        if (success)
                            server.execute(() -> ((PATLoadFromString) handler.player.getAdvancementTracker()).loadFromString(server.getAdvancementLoader(), resposne.body()));
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> BackendServerModule.asyncPost(AdvancementSyncMod.getUrl("unloadPlayer", handler.player)));
    }

    @Override
    public AdvancementSyncConfig createDefaultConfig() {
        return new AdvancementSyncConfig();
    }

    public static void syncGrantedAdvancement(ServerPlayerEntity serverPlayerEntity, String json) {
        try {
            BackendServerModule.asyncPost(getUrl("addAdvancements", serverPlayerEntity), json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void syncRevokedAdvancement(ServerPlayerEntity serverPlayerEntity, String json) {
        try {
            BackendServerModule.asyncPost(getUrl("removeAdvancements", serverPlayerEntity), json);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String getUrl(String route, ServerPlayerEntity player) {
        return route + "/" + ((PersonatusProfile) player.getGameProfile()).getRealProfile().getId().toString();
    }
}