package net.kyrptonaught.serverutils.backendServer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.kyrptonaught.serverutils.ModuleWConfig;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.discordBridge.MessageSender;

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

    @Override
    public void onInitialize() {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        client = HttpClient.newBuilder()
                .executor(executorService)
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> executorService.shutdown());
    }

    public static void asyncPost(String url, BiConsumer<Boolean, HttpResponse<String>> response) {
        HttpRequest request = buildPostRequest(getApiUrl(url));
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(stringHttpResponse -> response.accept(didRequestPass(stringHttpResponse), stringHttpResponse));
    }

    public static void asyncGet(String url, BiConsumer<Boolean, HttpResponse<String>> response) {
        HttpRequest request = buildGetRequest(getApiUrl(url));
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(stringHttpResponse -> response.accept(didRequestPass(stringHttpResponse), stringHttpResponse));
    }

    public static void asyncPost(String url, String json, BiConsumer<Boolean, HttpResponse<String>> response) {
        HttpRequest request = buildPostRequest(getApiUrl(url), json);
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenAccept(stringHttpResponse -> response.accept(didRequestPass(stringHttpResponse), stringHttpResponse));
    }

    public static void asyncPost(String url, String json) {
        HttpRequest request = buildPostRequest(getApiUrl(url), json);
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
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }

        return didRequestPass(response) ? response.body() : null;
    }

    private static String getApiUrl(String module) {
        return getApiURL() + "/" + module;
    }

    private static String getApiURL() {
        BackendServerConfig config = ServerUtilsMod.BackendModule.getConfig();
        return config.apiUrl + "/v0/" + config.secretKey;
    }

    private static HttpRequest buildPostRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
    }

    private static HttpRequest buildPostRequest(String url, String json) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
    }

    private static HttpRequest buildGetRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(2))
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
}
