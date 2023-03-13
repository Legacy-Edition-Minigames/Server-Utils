package net.kyrptonaught.serverutils.discordBridge.bot;

import net.kyrptonaught.serverutils.backendServer.BackendServerModule;
import net.kyrptonaught.serverutils.discordBridge.DiscordBridgeMod;

public class WebhookSender {
    private static final String mentions = "\"parse\":[\"users\",\"roles\",\"everyone\"]", noMentions = "\"parse\":[]";

    public static void sendMessage(String name, String url, String msg) {
        sendMessage(name, url, msg, false);
    }

    public static void sendMessage(String name, String url, String msg, boolean allowMentions) {
        String webhookUrl = DiscordBridgeMod.config().webhookURL;
        if (webhookUrl != null) {
            String payload = "{" +
                    "\"content\":\"" + msg + "\"," +
                    "\"username\":\"" + name + "\"," +
                    "\"avatar_url\":\"" + url + "\"," +
                    "\"allowed_mentions\": {" +
                    (allowMentions ? mentions : noMentions) +
                    "}}";
            BackendServerModule.asyncPostAlt(webhookUrl, payload);
        }
    }

    public static void log(String logSource, String message) {
        String webhookUrl = DiscordBridgeMod.config().loggingWebhookURL;
        if (webhookUrl != null) {
            String payload = "{\"embeds\":[{" +
                    "\"title\":\"" + logSource + "\"," +
                    "\"description\":\"" + message + "\"," +
                    "\"color\":\"" + 0xa87132 + "\"" +
                    "}]}";
            BackendServerModule.asyncPostAlt(webhookUrl, payload);
        }
    }

    public static void logMention(String logSource, String message, boolean allowMentions) {
        String webhookUrl = DiscordBridgeMod.config().loggingWebhookURL;
        if (webhookUrl != null) {
            String payload = "{" +
                    "\"content\":\"" + "**" + logSource + "** <@&" + DiscordBridgeMod.config().moderatorRoleID + ">\\n" + message + "\"," +
                    "\"allowed_mentions\": {" +
                    (allowMentions ? mentions : noMentions) +
                    "}}";
            BackendServerModule.asyncPostAlt(webhookUrl, payload);
        }
    }
}
