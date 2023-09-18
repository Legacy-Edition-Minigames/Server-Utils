package net.kyrptonaught.serverutils.discordBridge;

import net.kyrptonaught.serverutils.discordBridge.bot.WebhookSender;
import net.kyrptonaught.serverutils.discordBridge.format.FormatToDiscord;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class MessageSender {

    public static void sendChatMessage(ServerPlayerEntity player, String message) {
        if (DiscordBridgeMod.config().webhookURL != null)
            WebhookSender.sendMessage(player.getEntityName(), DiscordBridgeMod.getUserHeadURL(player), FormatToDiscord.toDiscord(player.getServer(), message));
    }

    public static void sendGameMessageWMentions(Text message) {
        if (DiscordBridgeMod.bot != null) {
            DiscordBridgeMod.bot.sendMessage(FormatToDiscord.toDiscord(DiscordBridgeMod.bot.server, message, true), canDiscordMSGCMDPing());
        }
    }

    public static void sendGameMessage(String message, int color) {
        if (DiscordBridgeMod.bot != null) {
            DiscordBridgeMod.bot.sendEmbed(null, FormatToDiscord.toDiscord(DiscordBridgeMod.bot.server, message, true), color);
        }
    }

    public static void sendGameMessage(Text message, int color) {
        if (DiscordBridgeMod.bot != null) {
            DiscordBridgeMod.bot.sendEmbed(null, FormatToDiscord.toDiscord(DiscordBridgeMod.bot.server, message, true), color);
        }
    }

    public static void sendLogMessage(String message) {
        WebhookSender.log(serverName(), FormatToDiscord.escapeFormatting(message));
    }

    public static void sendLogWMentions(String message) {
        WebhookSender.logMention(serverName(), FormatToDiscord.escapeFormatting(message), true);
    }

    private static String serverName() {
        return DiscordBridgeMod.config().serverName;
    }


    private static boolean canDiscordMSGCMDPing() {
        return DiscordBridgeMod.config().canDiscordMSGCMDPing;
    }
}
