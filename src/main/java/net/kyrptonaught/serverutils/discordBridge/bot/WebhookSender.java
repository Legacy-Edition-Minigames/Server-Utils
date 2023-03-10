package net.kyrptonaught.serverutils.discordBridge.bot;

import net.kyrptonaught.serverutils.backendServer.BackendServerModule;
import net.kyrptonaught.serverutils.discordBridge.DiscordBridgeMod;

public class WebhookSender {
    private static final String payloadTemplate = " {\"content\":\"%1$s\",\"username\":\"%2$s\",\"avatar_url\":\"%3$s\",\"allowed_mentions\":{%4$s}}";

    public static void sendMessage(String name, String url, String msg) {
        sendMessage(name, url, msg, false);
    }

    public static void sendMessage(String name, String url, String msg, boolean allowMentions) {
        String webhookUrl = DiscordBridgeMod.config().webhookURL;
        if (webhookUrl != null) {
            String payload = payloadTemplate.formatted(msg, name, url, allowMentions ? " \"parse\":[\"users\",\"roles\",\"everyone\"]" : "\"parse\": []");
            BackendServerModule.asyncPostAlt(webhookUrl, payload);
        }
    }

    public static void log(String logSource, String message) {
        String webhookUrl = DiscordBridgeMod.config().loggingWebhookURL;
        if (webhookUrl != null) {
            String payload = """
                    {
                    "embeds" :
                        [
                            {
                                "title" : "%1$s",
                                "description" :"%2$s",
                                "color" : %3$s
                            }
                        ]
                    }
                    """.formatted(logSource, message, 0xa87132);
            BackendServerModule.asyncPostAlt(webhookUrl, payload);
        }
    }
}
