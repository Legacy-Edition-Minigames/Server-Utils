package net.kyrptonaught.serverutils.discordBridge.bot;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.kyrptonaught.serverutils.discordBridge.DiscordBridgeMod;
import net.kyrptonaught.serverutils.discordBridge.format.FormatToMC;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.function.Consumer;

public class BridgeBot extends ListenerAdapter {
    public final MinecraftServer server;
    public final JDA jda;
    public final WebhookClient client;

    private final HashMap<String, Consumer<SlashCommandInteraction>> commands = new HashMap<>();

    public BridgeBot(MinecraftServer server, JDA jda, WebhookClient webhookClient) {
        this.server = server;
        this.jda = jda;
        this.client = webhookClient;
        this.jda.addEventListener(this);
    }

    public void registerCommand(String cmd, String description, Consumer<SlashCommandInteraction> execute) {
        registerCommand(Commands.slash(cmd, description).setGuildOnly(true), execute);
    }

    public void registerCommand(SlashCommandData commandData, Consumer<SlashCommandInteraction> execute) {
        this.jda.updateCommands().addCommands(commandData).queue();
        this.commands.put(commandData.getName(), execute);
    }

    public void sendMessage(String name, String url, String msg) {
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setUsername(name);
        builder.setAvatarUrl(url);
        builder.setContent(msg);
        client.send(builder.build());
    }

    public void sendEmbed(String name, String url, String msg, String title, int hexColor) {
        WebhookMessageBuilder builder = new WebhookMessageBuilder();
        builder.setUsername(name);
        builder.setAvatarUrl(url);
        WebhookEmbedBuilder embedBuilder = new WebhookEmbedBuilder();
        if (title != null) embedBuilder.setTitle(new WebhookEmbed.EmbedTitle(title, null));
        if (hexColor != 0) embedBuilder.setColor(hexColor);
        embedBuilder.setDescription(msg);

        client.send(builder.addEmbeds(embedBuilder.build()).build());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isWebhookMessage() && isAllowedChannel(event.getChannel().getIdLong())) {
            if (event.getMessage().getReferencedMessage() != null) {
                Text message = FormatToMC.parseMessage(event.getMessage().getReferencedMessage(), Text.literal("    ┌──── ").formatted(Formatting.GRAY));
                server.getPlayerManager().broadcast(message, false);
            }

            Text message = FormatToMC.parseMessage(event.getMessage(), Text.literal("[Discord] ").formatted(Formatting.BLUE));
            server.getPlayerManager().broadcast(message, false);

        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (commands.containsKey(event.getName()) && isAllowedChannel(event.getChannel().getIdLong()))
            commands.get(event.getName()).accept(event);
    }

    public void log(String message) {
        long channel = DiscordBridgeMod.config().loggingChannelID;
        if (channel != 0) {
            jda.getTextChannelById(DiscordBridgeMod.config().loggingChannelID).sendMessageEmbeds(
                    new EmbedBuilder()
                            .setDescription(message)
                            .setColor(0xa87132)
                            .build()).queue();
        }
    }

    public void lockChannel(boolean locked) {
        if (locked) {
            jda.getTextChannelById(getChannel()).upsertPermissionOverride(jda.getRoleById(DiscordBridgeMod.config().linkRoleID))
                    .deny(Permission.MESSAGE_SEND).queue();
        } else {
            jda.getTextChannelById(getChannel()).upsertPermissionOverride(jda.getRoleById(DiscordBridgeMod.config().linkRoleID))
                    .grant(Permission.MESSAGE_SEND).queue();
        }
    }


    public boolean isAllowedChannel(long channel) {
        return getChannel() == channel;
    }

    public long getChannel() {
        return DiscordBridgeMod.config().channelID;
    }

    public void close() {
        if (jda != null) jda.shutdown();
        if (client != null) client.close();
    }
}
