package net.kyrptonaught.serverutils.discordBridge.linking;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.kyrptonaught.serverutils.backendServer.BackendServerModule;
import net.kyrptonaught.serverutils.discordBridge.DiscordBridgeMod;
import net.kyrptonaught.serverutils.discordBridge.MessageSender;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.RandomStringUtils;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class LinkingManager {
    public static HttpClient client;

    public static HashMap<String, ServerPlayerEntity> linksInProgress = new HashMap<>();

    public static void init() {
        client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    public static String beginLink(ServerPlayerEntity player) {
        String linkID = RandomStringUtils.randomAlphanumeric(5).toLowerCase();

        linksInProgress.put(linkID, player);
        return linkID;
    }

    public static void generateLink(String linkID, String discordID, SlashCommandInteractionEvent event) {
        if (!linksInProgress.containsKey(linkID)) {
            event.getHook().editOriginal("Error").queue();
            return;
        }

        HttpRequest request = BackendServerModule.buildPostRequest(BackendServerModule.getApiUrl("link/set/") + linksInProgress.get(linkID).getUuidAsString() + "/" + discordID);
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(stringHttpResponse -> {
                    if (BackendServerModule.didRequestFail(stringHttpResponse)) {
                        event.getHook().editOriginal("Error").queue();
                        return;
                    }

                    event.getHook().editOriginal("Linked!").queue();
                    event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(DiscordBridgeMod.config().linkRoleID)).queue();

                    ServerPlayerEntity player = linksInProgress.remove(linkID);
                    player.sendMessage(Text.empty().append(Text.literal("[Discord] ").formatted(Formatting.BLUE)).append("Linked Successfully!"));
                    MessageSender.sendLogMessage(event.getMember().getEffectiveName() + " linked their account to MC -> " + player.getDisplayName() + "(" + player.getUuidAsString() + ")");
                });
    }
}
