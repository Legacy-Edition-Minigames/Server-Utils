package net.kyrptonaught.serverutils.discordBridge;

import net.kyrptonaught.serverutils.backendServer.BackendServerModule;
import net.kyrptonaught.serverutils.chatDisabler.ChatDisablerConfig;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class Integrations {

    public static void chatDisabler(String user, boolean enabled, ChatDisablerConfig config) {
        if (DiscordBridgeMod.bot != null) {
            DiscordBridgeMod.bot.lockChannel(!enabled);
            MessageSender.sendLogMessage(user + " set chat to " + enabled);
            if (enabled) {
                if (config.notifyChatEnabled) MessageSender.sendGameMessage(config.enabledMessage, 0x32a852);
            } else if (config.notifyChatDisabled) MessageSender.sendGameMessage(config.disabledMessage, 0xa83832);
        }
    }

    public static void personatusEnable(String user, boolean enabled) {
        MessageSender.sendLogMessage(user + " set personatus to " + enabled);

    }

    public static void personatusSpoof(String user, String player, String spoof) {
        MessageSender.sendLogMessage(user + " set " + player + "'s personatus to " + spoof);
    }

    public static void personatusClear(String user, String player) {
        MessageSender.sendLogMessage(user + " cleared " + player + "'s personatus");
    }

    public static void sendJoinMessage(ServerPlayerEntity player, Text message) {
        MessageSender.sendGameMessage(Text.literal("➡️ ").append(message), 0x6332a8);
        if (DiscordBridgeMod.config().loggingWebhookURL != null) {
            BackendServerModule.asyncGet("link/sus/check/" + player.getUuidAsString().replaceAll("-", ""), (success, response) -> {
                if (success && "true".equals(response.body())) {
                    MessageSender.sendLogWMentions("A sussy baka joined the server: " + player.getEntityName());
                }
            });
        }
    }

    public static void sendLeaveMessage(Text message) {
        MessageSender.sendGameMessage(Text.literal("⬅️ ").append(message), 0x6332a8);
    }

    public static void sendDeathMessage(Text message) {
        MessageSender.sendGameMessage(message, 0x8c0a0a);
    }

    public static void sendAdvancementMessage(Text message) {
        MessageSender.sendGameMessage(Text.literal("⭐ ").append(message), 0x0a728c);
    }
}
