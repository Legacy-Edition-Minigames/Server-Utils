package net.kyrptonaught.serverutils.discordBridge;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class MessageSender {

    public static void sendChatMessage(ServerPlayerEntity player, String message) {
        if (DiscordBridgeMod.bot != null)
            DiscordBridgeMod.bot.sendMessage(player.getEntityName(), DiscordBridgeMod.getUserHeadURL(player), TextToDiscordFormatter.toDiscord(DiscordBridgeMod.bot, message));
    }

    public static void sendGameMessage(Text message) {
        if (DiscordBridgeMod.bot != null)
            DiscordBridgeMod.bot.sendMessage(DiscordBridgeMod.config().GameMessageName, DiscordBridgeMod.config().GameMessageAvatarURL, TextToDiscordFormatter.toDiscord(DiscordBridgeMod.bot, message));
    }

    public static void sendGameMessage(String message) {
        if (DiscordBridgeMod.bot != null) {
            DiscordBridgeMod.bot.sendMessage(DiscordBridgeMod.config().GameMessageName, DiscordBridgeMod.config().GameMessageAvatarURL, TextToDiscordFormatter.toDiscord(DiscordBridgeMod.bot, message));
        }
    }

    public static void sendJoinLeaveMessage(Text message) {
        sendGameMessage(message);
    }

    public static void sendDeathMessage(Text message) {
        sendGameMessage(message);
    }

    public static void sendAdvancementMessage(Text message) {
        sendGameMessage(message);
    }
}
