package net.kyrptonaught.serverutils.backendLink;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.kyrptonaught.serverutils.ConfigManager;
import net.kyrptonaught.serverutils.backendLink.discordBridge.DiscordBridge;
import net.kyrptonaught.serverutils.backendLink.personatus.PersonatusModule;
import net.kyrptonaught.serverutils.backendLink.prohibitor.ProhibitorModule;
import net.kyrptonaught.serverutils.serverTranslator.ServerTranslator;
import net.kyrptonaught.serverutils.userConfig.UserConfigStorage;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

public class BackendServer {
    public static BackendServerConfig config;
    private static HttpClient client;
    private static ExecutorService executorService;


    public static void onPreLaunch() {
        config = ConfigManager.readFileJson("backend.json5", BackendServerConfig.class);

        executorService = Executors.newFixedThreadPool(2);
        client = HttpClient.newBuilder()
                .executor(executorService)
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        DiscordBridge.earlyInit();
    }

    public static void init() {
        DiscordBridge.init();
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> executorService.shutdown());
    }


    public static GameProfile earlyLogin(ServerLoginNetworkHandler handler, GameProfile profile) {
        JsonObject result = ProhibitorModule.canJoin(handler, profile);
        if (result == null) {
            handler.disconnect(ServerTranslator.translate(Text.translatable("disconnect.backend")));
            //VelocityProxyHelper.kickPlayer(handler, profile, ServerTranslator.translate(Text.translatable("disconnect.backend")));
            return profile;
        }

        if (result.get("isBanned").getAsBoolean()) {
            Text reason = ServerTranslator.translate(TextCodecs.CODEC.parse(JsonOps.INSTANCE, result.get("banMessage")).result().get());
            handler.disconnect(ServerTranslator.translate(reason));
            //VelocityProxyHelper.kickPlayer(handler, profile, ServerTranslator.translate(reason));
            return profile;
        }

        UserConfigStorage.loadPlayer(profile);
        for (String s : result.keySet()) {
            UserConfigStorage.setValue(profile.getId(), new Identifier(s.toLowerCase()), result.get(s).toString());
        }

        profile = ProhibitorModule.checkProfile(handler, profile, result);
        profile = PersonatusModule.checkProfile(handler, profile, result);

        return profile;
    }

    public static void asyncPost(String url, BiConsumer<Boolean, HttpResponse<String>> response) {
        HttpRequest request = buildPostRequest(getApiUrl(url));
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .exceptionally(throwable -> null)
                .thenAccept(stringHttpResponse -> response.accept(didRequestPass(stringHttpResponse), stringHttpResponse));
    }

    public static void asyncGet(String url, BiConsumer<Boolean, HttpResponse<String>> response) {
        HttpRequest request = buildGetRequest(getApiUrl(url));
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .exceptionally(throwable -> null)
                .thenAccept(stringHttpResponse -> response.accept(didRequestPass(stringHttpResponse), stringHttpResponse));
    }

    public static void asyncPost(String url, String json, BiConsumer<Boolean, HttpResponse<String>> response) {
        HttpRequest request = buildPostRequest(getApiUrl(url), json);
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .exceptionally(throwable -> null)
                .thenAccept(stringHttpResponse -> response.accept(didRequestPass(stringHttpResponse), stringHttpResponse));
    }

    public static void asyncPost(String url, String json) {
        HttpRequest request = buildPostRequest(getApiUrl(url), json);
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public static void asyncPost(String url) {
        HttpRequest request = buildPostRequest(getApiUrl(url));
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public static void asyncPostAlt(String url, String json) {
        HttpRequest request = buildPostRequest(url, json);
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }

    public static String get(String url) {
        return getAlt(getApiUrl(url));
    }

    public static String get(String url, String json) {
        return getAlt(getApiUrl(url), json);
    }

    public static String getAlt(String url) {
        HttpRequest request = buildGetRequest(url);
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ignored) {
            return null;
        }

        return didRequestPass(response) ? response.body() : null;
    }

    public static String getAlt(String url, String json) {
        HttpRequest request = buildGetRequest(url, json);
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ignored) {
            return null;
        }

        return didRequestPass(response) ? response.body() : null;
    }

    public static boolean post(String url) {
        HttpRequest request = buildPostRequest(getApiUrl(url));
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ignored) {
            return false;
        }

        return didRequestPass(response);
    }

    public static String getApiUrl(String module) {
        if (module.contains("Advancements") || module.contains("UserConfig") || module.contains("unloadPlayer"))
            return getApiURL().replace("/v1/", "/v0/") + "/" + module;
        return getApiURL() + "/" + module;
    }

    public static String getApiURL() {
        return config.apiUrl + "/v1/" + config.secretKey;
    }

    private static HttpRequest buildPostRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
    }

    private static HttpRequest buildPostRequest(String url, String json) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
    }

    private static HttpRequest buildGetRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .GET()
                .build();
    }

    private static HttpRequest buildGetRequest(String url, String json) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .method("GET", HttpRequest.BodyPublishers.ofString(json))
                .build();
    }

    private static boolean didRequestPass(HttpResponse<String> response) {
        return response != null && response.statusCode() == 200 && !response.body().equalsIgnoreCase("failed");
    }
}
