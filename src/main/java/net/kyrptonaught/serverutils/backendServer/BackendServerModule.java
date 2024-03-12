package net.kyrptonaught.serverutils.backendServer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.kyrptonaught.LEMBackend.LEMBackend;
import net.kyrptonaught.serverutils.ModuleWConfig;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.personatus.PersonatusProfile;
import net.minecraft.server.network.ServerPlayerEntity;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

public class BackendServerModule extends ModuleWConfig<BackendServerConfig> {
    private static HttpClient client;

    public BackendServerModule(String MOD_ID) {
        setMOD_ID(MOD_ID);
    }

    @Override
    public void onInitialize() {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        client = HttpClient.newBuilder()
                .executor(executorService)
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> executorService.shutdown());

        if (ServerUtilsMod.backendModule.getConfig().runBackendServer) {
            ServerLifecycleEvents.SERVER_STOPPED.register(server -> LEMBackend.shutdown());
            ServerLifecycleEvents.SERVER_STARTED.register(LEMBackend::start);
        }
    }

    public static boolean backendRunning() {
        return LEMBackend.app != null;
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

    private static String getApiUrl(String module) {
        return getApiURL() + "/" + module;
    }

    private static String getApiURL() {
        BackendServerConfig config = ServerUtilsMod.backendModule.getConfig();
        return config.apiUrl + "/v0/" + config.secretKey;
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

    private static boolean didRequestPass(HttpResponse<String> response) {
        return response != null && response.statusCode() == 200 && !response.body().equalsIgnoreCase("failed");
    }

    @Override
    public BackendServerConfig createDefaultConfig() {
        return new BackendServerConfig();
    }

    public static String getUrl(String route, ServerPlayerEntity player) {
        return route + "/" + ((PersonatusProfile) player.getGameProfile()).getRealProfile().getId().toString();
    }
}
