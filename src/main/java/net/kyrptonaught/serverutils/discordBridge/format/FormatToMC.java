package net.kyrptonaught.serverutils.discordBridge.format;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class FormatToMC {

    public static Text parseMessage(Message discordMessage, MutableText prefix) {
        HashMap<String, String> replacementURLs = new HashMap<>();

        for (MessageEmbed embed : discordMessage.getEmbeds())
            replacementURLs.put(embed.getUrl(), embed.getUrl());
        for (Message.Attachment attachment : discordMessage.getAttachments())
            replacementURLs.put(attachment.getFileName(), attachment.getUrl());

        int color = discordMessage.getMember().getColorRaw();
        MutableText message = Text.literal("").append(prefix).append(Text.literal("<" + discordMessage.getAuthor().getName() + "> ").styled(style -> style.withColor(color)));
        for (String str : discordMessage.getContentDisplay().split(" ")) {
            message.append(parseText(str, replacementURLs));
        }

        if (replacementURLs.size() > 0) {
            Iterator<String> iterator = replacementURLs.keySet().iterator();
            message.append("{");
            while (iterator.hasNext()) {
                String str = iterator.next();
                message.append(Text.literal(str + (iterator.hasNext() ? ", " : "")).setStyle(styleURL(replacementURLs.get(str))));
            }
            message.append("}");
        }

        return message;
    }

    private static void format(List<Text> texts, String message) {
        int underBegin = message.indexOf("__");
        int underEnd = message.indexOf("__", underBegin + 1);
        if (underBegin > -1 && underEnd > -1) {
            texts.add(Text.literal(message.substring(0, underBegin)));
            texts.add(Text.literal(message.substring(underBegin + 2, underEnd)).styled(style -> style.withUnderline(true)));
            texts.add(Text.literal(message.substring(underEnd + 2)));
        } else texts.add(Text.literal(message));
    }

    private static Text parseText(String text, HashMap<String, String> replacementURLs) {
        if (!replacementURLs.containsKey(text))
            return Text.literal(text + " ");

        return Text.literal(text + " ").setStyle(styleURL(replacementURLs.remove(text)));
    }

    private static Style styleURL(String url) {
        return Style.EMPTY.withFormatting(Formatting.UNDERLINE, Formatting.BLUE).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
    }
}
