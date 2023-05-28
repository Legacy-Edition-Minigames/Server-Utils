package net.kyrptonaught.serverutils.discordBridge;

import net.kyrptonaught.serverutils.backendServer.BackendServerModule;
import net.kyrptonaught.serverutils.chatDisabler.ChatDisablerConfig;
import net.kyrptonaught.serverutils.discordBridge.format.FormatToDiscord;
import net.kyrptonaught.serverutils.personatus.PersonatusProfile;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class Integrations {

    public static void chatDisabler(ServerCommandSource source, boolean enabled, ChatDisablerConfig config) {
        if (DiscordBridgeMod.bot != null) {
            DiscordBridgeMod.bot.lockChannel(!enabled);
            MessageSender.sendLogMessage(getSenderName(source) + " set chat to " + enabled);
            if (enabled) {
                if (config.notifyChatEnabled) MessageSender.sendGameMessage(config.enabledMessage, 0x32a852);
            } else if (config.notifyChatDisabled) MessageSender.sendGameMessage(config.disabledMessage, 0xa83832);
        }
    }

    public static void personatusEnable(ServerCommandSource source, boolean enabled) {
        MessageSender.sendLogMessage(getSenderName(source) + " set personatus to " + enabled);
    }

    public static void personatusSpoof(ServerCommandSource source, String player, String spoof) {
        MessageSender.sendLogMessage(getSenderName(source) + " set " + player + "'s personatus to " + spoof);
    }

    public static void personatusClear(ServerCommandSource source, String player) {
        MessageSender.sendLogMessage(getSenderName(source) + " cleared " + player + "'s personatus");
    }

    public static void whitelistSync(ServerCommandSource source, Text text) {
        MessageSender.sendLogMessage(getSenderName(source) + ": " + FormatToDiscord.toDiscord(source.getServer(), text));
    }


    public static void sendJoinMessage(ServerPlayerEntity player, Text message) {
        MessageSender.sendGameMessage(Text.literal("➡️ ").append(message), 0x6332a8);
        if (DiscordBridgeMod.config().loggingWebhookURL != null) {
            BackendServerModule.asyncGet("link/sus/check/" + player.getUuidAsString().replaceAll("-", ""), (success, response) -> {
                if (success && "true".equals(response.body())) {
                    MessageSender.sendLogWMentions("A suspicous player joined the server: " + player.getEntityName());
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

    public static String getSenderName(ServerCommandSource source) {
        if (source.isExecutedByPlayer() && source.getPlayer() != null) {
            PersonatusProfile profile = ((PersonatusProfile) source.getPlayer().getGameProfile());
            if (profile.isSpoofed())
                return source.getName() + "(" + profile.getRealProfile().getName() + ")";
        }
        return source.getName();
    }
}
