package net.kyrptonaught.serverutils.advancementSync;

import blue.endless.jankson.JsonElement;
import com.google.gson.JsonObject;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class AdvancementSyncMod {
    public static String MOD_ID = "advancementsync";

    public static void onInitialize() {
        ServerUtilsMod.configManager.registerFile(MOD_ID, new AdvancementSyncConfig());
    }

    public static AdvancementSyncConfig getConfig() {
        return (AdvancementSyncConfig) ServerUtilsMod.configManager.getConfig(MOD_ID);
    }


    public static void syncGrantedAdvancement(ServerPlayerEntity serverPlayerEntity, String json) {
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build();) {
            HttpPost request = new HttpPost(getConfig().apiUrl + "/addAdvancements/" + getConfig().secretKey + "/" + serverPlayerEntity.getUuidAsString());
            request.setHeader("Content-type", "application/json");
            request.setEntity(new StringEntity(json));
            HttpResponse response = httpClient.execute(request);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void syncRevokedAdvancement(ServerPlayerEntity serverPlayerEntity, JsonObject json) {
        System.out.println(json);

        /*
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build();) {
            HttpPost request = new HttpPost(getConfig().apiUrl + "/addAdvancements/" + getConfig().secretKey + "/" + serverPlayerEntity.getUuidAsString());
            request.setHeader("Content-type", "application/json");
            request.setEntity(new StringEntity(json));
            HttpResponse response = httpClient.execute(request);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

         */
    }
}
