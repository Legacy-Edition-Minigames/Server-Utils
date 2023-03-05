package net.kyrptonaught.serverutils.discordBridge.format;

import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.kyrptonaught.serverutils.discordBridge.DiscordBridgeMod;
import net.kyrptonaught.serverutils.discordBridge.bot.BridgeBot;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;

import java.util.Optional;

public class FormatToDiscord {

    public static String toDiscord(BridgeBot bot, String text) {
        for (RichCustomEmoji emoji : DiscordBridgeMod.bot.jda.getEmojiCache())
            text = text.replaceAll(":" + emoji.getName() + ":", emoji.getAsMention());
        return text;
    }

    public static String toDiscord(BridgeBot bot, Text text) {
        try {
            text = Texts.parse(bot.server.getCommandSource(), text, null, 0);
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

            output.append(modifier).append(text2).append(modifier.reverse());
            return Optional.empty();
        }, text.getStyle());

        return toDiscord(bot, output.toString());
    }
}