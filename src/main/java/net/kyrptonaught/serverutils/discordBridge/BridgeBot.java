package net.kyrptonaught.serverutils.discordBridge;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.function.Consumer;

public class BridgeBot extends ListenerAdapter {
    protected final MinecraftServer server;
    protected final JDA jda;
    protected final WebhookClient client;
    protected final String channelID;

    private final HashMap<String, Consumer<SlashCommandInteraction>> commands = new HashMap<>();

    public BridgeBot(MinecraftServer server, JDA jda, WebhookClient webhookClient, String channelID) {
        this.server = server;
        this.jda = jda;
        this.client = webhookClient;
        this.channelID = channelID;
        this.jda.addEventListener(this);
    }

    public void registerCommand(String cmd, String description, Consumer<SlashCommandInteraction> execute) {
        this.jda.updateCommands().addCommands(Commands.slash(cmd, description).setGuildOnly(true)).queue();
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
                Text message = DiscordToTextFormatter.parseMessage(event.getMessage().getReferencedMessage(), Text.literal("    ┌──── ").formatted(Formatting.GRAY));
                server.getPlayerManager().broadcast(message,false);
            }

            Text message = DiscordToTextFormatter.parseMessage(event.getMessage(), Text.literal("[Discord] ").formatted(Formatting.BLUE));
            server.getPlayerManager().broadcast(message,false);
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (commands.containsKey(event.getName()) && event.getChannel().getId().equals(channelID))
            commands.get(event.getName()).accept(event);
    }

    public void close() {
        if (jda != null) jda.shutdown();
        if (client != null) client.close();
    }
}
