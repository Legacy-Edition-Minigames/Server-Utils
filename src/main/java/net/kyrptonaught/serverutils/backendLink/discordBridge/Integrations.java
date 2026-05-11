package net.kyrptonaught.serverutils.backendLink.discordBridge;


import net.kyrptonaught.serverutils.backendLink.personatus.PersonatusProfile;
import net.kyrptonaught.serverutils.userConfig.UserConfigStorage;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class Integrations {

    public static void chatDisabler(String source, boolean enabled) {
        DiscordBridge.sendLogMessage(source + " set chat to " + enabled);

        if (enabled) DiscordBridge.sendMessage(Text.translatable("chatdisabler.enablechat.enabled"), 0x32a852);
        else DiscordBridge.sendMessage(Text.translatable("chatdisabler.enablechat.disabled"), 0xa83832);

        DiscordBridge.sendLockChannel(!enabled);
    }

    public static void linkingStatus(String source, boolean enabled) {
        DiscordBridge.sendLogMessage(source + " set linking to " + enabled);
    }

    public static void personatusEnable(ServerCommandSource source, boolean enabled) {
        DiscordBridge.sendLogMessage(getSenderName(source) + " set personatus to " + enabled);
    }

    public static void personatusSpoof(ServerCommandSource source, String player, String spoof) {
        DiscordBridge.sendLogMessage(getSenderName(source) + " set " + player + "'s personatus to " + spoof);
    }

    public static void personatusClear(ServerCommandSource source, String player) {
        DiscordBridge.sendLogMessage(getSenderName(source) + " cleared " + player + "'s personatus");
    }

    public static void sendJoinMessage(ServerPlayerEntity player, Text message) {
        DiscordBridge.sendMessage(Text.literal("➡️ ").append(message), 0x6332a8);
        if ("true".equals(UserConfigStorage.getValue(player, new Identifier("lem.base", "suspicious"))))
            DiscordBridge.sendLogMessage(Text.literal("A suspicious player joined the server: **" + player.getNameForScoreboard() + "**"), true);
    }

    public static void sendLeaveMessage(Text message) {
        DiscordBridge.sendMessage(Text.literal("⬅️ ").append(message), 0x6332a8);
    }

    public static void sendDeathMessage(Text message) {
        DiscordBridge.sendMessage(message, 0x8c0a0a);
    }

    public static void sendAdvancementMessage(Text message) {
        DiscordBridge.sendMessage(Text.literal("⭐ ").append(message), 0x0a728c);
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
