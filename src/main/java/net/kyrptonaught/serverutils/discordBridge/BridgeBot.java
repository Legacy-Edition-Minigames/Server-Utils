package net.kyrptonaught.serverutils.discordBridge;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Iterator;
import java.util.function.Consumer;

public class BridgeBot extends ListenerAdapter {
    private final MinecraftServer server;
    private final JDA jda;
    private final WebhookClient client;
    private final String channelID;

    private final HashMap<String, Consumer<SlashCommandInteraction>> commands = new HashMap<>();

    public BridgeBot(MinecraftServer server, JDA jda, WebhookClient webhookClient, String channelID) {
        this.server = server;
        this.jda = jda;
        this.client = webhookClient;
        this.channelID = channelID;
        this.jda.addEventListener(this);
    }

    public void registerCommand(String cmd, String description, Consumer<SlashCommandInteraction> execute) {
        this.jda.updateCommands().addCommands(Commands.slash(cmd, description)).queue();
        this.commands.put(cmd, execute);
    }

    public void sendMessage(String name, String url, String msg) {
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setUsername(name);
        builder.setAvatarUrl(url);
        builder.setContent(msg);
        client.send(builder.build());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isWebhookMessage() && event.getChannel().getId().equals(channelID)) {
            if (event.getMessage().getReferencedMessage() != null) {
                parseMessage(event.getMessage().getReferencedMessage(), Text.literal("    ┌──── ").formatted(Formatting.GRAY));
            }

            parseMessage(event.getMessage(), Text.literal("[Discord] ").formatted(Formatting.BLUE));
        }
    }

    public void parseMessage(Message discordMessage, MutableText prefix) {
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

        DiscordBridgeMod.sendToAll(server, message);
    }

    public Text parseText(String text, HashMap<String, String> replacementURLs) {
        if (!replacementURLs.containsKey(text))
            return Text.literal(text + " ");

        return Text.literal(text + " ").setStyle(styleURL(replacementURLs.remove(text)));
    }

    public Style styleURL(String url) {
        return Style.EMPTY.withFormatting(Formatting.UNDERLINE, Formatting.BLUE).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (commands.containsKey(event.getName()))
            commands.get(event.getName()).accept(event);
    }

    public void close() {
        if (jda != null) jda.shutdown();
        if (client != null) client.close();
    }
}
