package net.kyrptonaught.serverutils.backendServer;

import net.kyrptonaught.serverutils.ModuleWConfig;
import net.kyrptonaught.serverutils.ServerUtilsMod;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class BackendServerModule extends ModuleWConfig<BackendServerConfig> {

    public static String getApiUrl(String module){
        return getApiURL() + "/"+module;
    }

    public static String getApiURL() {
        BackendServerConfig config = ServerUtilsMod.BackendModule.getConfig();

        return config.apiUrl + "/v0/" + config.secretKey;
    }

    public static HttpRequest buildPostRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
    }

    public static HttpRequest buildPostRequest(String url, String json) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
    }

    public static HttpRequest buildGetRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(2))
                .header("Content-Type", "application/json")
                .GET()
                .build();
    }

    public static boolean didRequestFail(HttpResponse<String> response) {
        return response == null || response.statusCode() != 200 || response.body().equalsIgnoreCase("failed");
    }

    @Override
    public BackendServerConfig createDefaultConfig() {
        return new BackendServerConfig();
    }
}
