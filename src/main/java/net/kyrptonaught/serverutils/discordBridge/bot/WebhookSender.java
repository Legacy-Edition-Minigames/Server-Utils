package net.kyrptonaught.serverutils.discordBridge.bot;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kyrptonaught.serverutils.backendServer.BackendServerModule;
import net.kyrptonaught.serverutils.discordBridge.DiscordBridgeMod;

public class WebhookSender {
    public static void sendMessage(String name, String url, String msg) {
        sendMessage(name, url, msg, false);
    }

    public static void sendMessage(String name, String url, String msg, boolean allowMentions) {
        String webhookUrl = DiscordBridgeMod.config().webhookURL;
        if (webhookUrl != null) {
            JsonObject payload = new JsonObject();
            payload.addProperty("content", msg);
            payload.addProperty("username", name);
            payload.addProperty("avatar_url", url);

            JsonObject mentions = new JsonObject();
            if (allowMentions) {
                JsonArray parse = new JsonArray();
                parse.add("users");
                parse.add("roles");
                parse.add("everyone");
                mentions.add("parse", parse);
            } else mentions.add("parse", new JsonArray());

            payload.add("allowed_mentions", mentions);
            BackendServerModule.asyncPostAlt(webhookUrl, payload.toString());
        }
    }

    public static void log(String logSource, String message) {
        String webhookUrl = DiscordBridgeMod.config().loggingWebhookURL;
        if (webhookUrl != null) {

            JsonObject embed = new JsonObject();
            embed.addProperty("title", logSource);
            embed.addProperty("description", message);
            embed.addProperty("color", 0xa87132);

            JsonArray embeds = new JsonArray();
            embeds.add(embed);

            JsonObject payload = new JsonObject();
            payload.add("embeds", embeds);

            BackendServerModule.asyncPostAlt(webhookUrl, payload.toString());
        }
    }

    public static void logMention(String logSource, String message, boolean allowMentions) {
        String webhookUrl = DiscordBridgeMod.config().loggingWebhookURL;
        if (webhookUrl != null) {

            JsonObject payload = new JsonObject();
            payload.addProperty("content", "**" + logSource + "** <@&" + DiscordBridgeMod.config().moderatorRoleID + ">\\n" + message);

            JsonObject mentions = new JsonObject();
            if (allowMentions) {
                JsonArray parse = new JsonArray();
                parse.add("users");
                parse.add("roles");
                parse.add("everyone");
                mentions.add("parse", parse);
            } else mentions.add("parse", new JsonArray());

            payload.add("allowed_mentions", mentions);

            BackendServerModule.asyncPostAlt(webhookUrl, payload.toString());
        }
    }
}
