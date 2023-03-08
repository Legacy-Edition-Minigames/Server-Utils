package net.kyrptonaught.serverutils.discordBridge;

import net.kyrptonaught.serverutils.discordBridge.format.FormatToDiscord;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class MessageSender {

    public static void sendChatMessage(ServerPlayerEntity player, String message) {
        if (DiscordBridgeMod.bot != null)
            DiscordBridgeMod.bot.sendMessage(player.getEntityName(), DiscordBridgeMod.getUserHeadURL(player), FormatToDiscord.toDiscord(DiscordBridgeMod.bot, message));
    }

    public static void sendGameMessage(Text message) {
        if (DiscordBridgeMod.bot != null)
            DiscordBridgeMod.bot.sendMessage(name(), url(), FormatToDiscord.toDiscord(DiscordBridgeMod.bot, message));
    }
    public static void sendGameMessageWMentions(Text message) {
        if (DiscordBridgeMod.bot != null)
            DiscordBridgeMod.bot.sendMessage(name(), url(), FormatToDiscord.toDiscord(DiscordBridgeMod.bot, message),true);
    }
    public static void sendGameMessage(String message) {
        if (DiscordBridgeMod.bot != null) {
            DiscordBridgeMod.bot.sendMessage(name(), url(), FormatToDiscord.toDiscord(DiscordBridgeMod.bot, message));
        }
    }

    public static void sendGameMessage(String message, int color) {
        if (DiscordBridgeMod.bot != null) {
            DiscordBridgeMod.bot.sendEmbed(name(), url(), FormatToDiscord.toDiscord(DiscordBridgeMod.bot, message), null, color);
        }
    }

    public static void sendGameMessage(Text message, int color) {
        if (DiscordBridgeMod.bot != null) {
            DiscordBridgeMod.bot.sendEmbed(name(), url(), FormatToDiscord.toDiscord(DiscordBridgeMod.bot, message), null, color);
        }
    }

    public static void sendLogMessage(String message) {
        if (DiscordBridgeMod.bot != null) {
            DiscordBridgeMod.bot.log(message);
        }
    }

    private static String name() {
        return DiscordBridgeMod.config().GameMessageName;
    }

    private static String url() {
        return DiscordBridgeMod.config().GameMessageAvatarURL;
    }
}
