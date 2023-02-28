package net.kyrptonaught.serverutils.advancementSync;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyrptonaught.serverutils.ModuleWConfig;
import net.kyrptonaught.serverutils.backendServer.BackendServerModule;
import net.kyrptonaught.serverutils.personatus.PersonatusProfile;
import net.minecraft.server.network.ServerPlayerEntity;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AdvancementSyncMod extends ModuleWConfig<AdvancementSyncConfig> {
    public static String MOD_ID = "advancementsync";
    public static HttpClient client;

    @Override
    public void onInitialize() {
        client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (getConfig().syncOnJoin) {
                try {
                    HttpRequest request = BackendServerModule.buildGetRequest(getUrl("getAdvancements", handler.player));

                    client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                            .thenAccept(stringHttpResponse -> {
                                if (!BackendServerModule.didRequestFail(stringHttpResponse)) {
                                    String json = stringHttpResponse.body();
                                    server.execute(() -> {
                                        ((PATLoadFromString) handler.player.getAdvancementTracker()).loadFromString(server.getAdvancementLoader(), json);
                                    });
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public AdvancementSyncConfig createDefaultConfig() {
        return new AdvancementSyncConfig();
    }

    public static void syncGrantedAdvancement(ServerPlayerEntity serverPlayerEntity, String json) {
        try {
            HttpRequest request = BackendServerModule.buildPostRequest(getUrl("addAdvancements", serverPlayerEntity), json);
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void syncRevokedAdvancement(ServerPlayerEntity serverPlayerEntity, String json) {
        try {
            HttpRequest request = BackendServerModule.buildPostRequest(getUrl("removeAdvancements", serverPlayerEntity), json);
            client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String getUrl(String route, ServerPlayerEntity player) {
        return BackendServerModule.getApiURL() + "/" + route + "/" + ((PersonatusProfile) player.getGameProfile()).getRealProfile().getId().toString();
    }

}
