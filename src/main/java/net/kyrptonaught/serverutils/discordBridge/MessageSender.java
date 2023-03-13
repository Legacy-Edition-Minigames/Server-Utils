package net.kyrptonaught.serverutils.discordBridge;

import net.kyrptonaught.serverutils.discordBridge.bot.WebhookSender;
import net.kyrptonaught.serverutils.discordBridge.format.FormatToDiscord;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class MessageSender {

    public static void sendChatMessage(ServerPlayerEntity player, String message) {
        WebhookSender.sendMessage(player.getEntityName(), DiscordBridgeMod.getUserHeadURL(player), FormatToDiscord.toDiscord(player.getServer(), message));
    }

    public static void sendGameMessageWMentions(Text message) {
        if (DiscordBridgeMod.bot != null) {
            DiscordBridgeMod.bot.sendMessage(FormatToDiscord.toDiscord(DiscordBridgeMod.bot.server, message));
        }
    }

    public static void sendGameMessage(String message, int color) {
        if (DiscordBridgeMod.bot != null) {
            DiscordBridgeMod.bot.sendEmbed(null, FormatToDiscord.toDiscord(DiscordBridgeMod.bot.server, message), color);
        }
    }

    public static void sendGameMessage(Text message, int color) {
        if (DiscordBridgeMod.bot != null) {
            DiscordBridgeMod.bot.sendEmbed(null, FormatToDiscord.toDiscord(DiscordBridgeMod.bot.server, message), color);
        }
    }

    public static void sendLogMessage(String message) {
        WebhookSender.log(serverName(), message);
    }

    public static void sendLogWMentions(String message) {
        WebhookSender.logMention(serverName(), message, true);
    }

    private static String serverName() {
        return DiscordBridgeMod.config().serverName;
    }

}
