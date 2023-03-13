package net.kyrptonaught.serverutils.discordBridge.format;

import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.kyrptonaught.serverutils.discordBridge.DiscordBridgeMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;

import java.util.Optional;

public class FormatToDiscord {
    public static String toDiscord(MinecraftServer server, String text) {
        if (DiscordBridgeMod.bot != null)
            for (RichCustomEmoji emoji : DiscordBridgeMod.bot.jda.getEmojiCache())
                text = text.replaceAll(":" + emoji.getName() + ":", emoji.getAsMention());
        return text;
    }

    public static String toDiscord(MinecraftServer server, Text text) {
        try {
            text = Texts.parse(server.getCommandSource(), text, null, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        StringBuilder output = new StringBuilder();
        text.visit((style, text2) -> {
            StringBuilder modifier = new StringBuilder();
            if (style.isUnderlined()) modifier.append("__");
            if (style.isStrikethrough()) modifier.append("~~");
            if (style.isItalic()) modifier.append("*");
            if (style.isBold()) modifier.append("**");

            text2 = text2.replaceAll("[\\uF801-\\uF880]", "");

            output.append(modifier).append(text2).append(modifier.reverse());
            return Optional.empty();
        }, text.getStyle());

        return toDiscord(server, output.toString());
    }
}