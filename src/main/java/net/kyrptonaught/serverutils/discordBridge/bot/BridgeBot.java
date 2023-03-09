package net.kyrptonaught.serverutils.discordBridge.bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.kyrptonaught.serverutils.backendServer.BackendServerModule;
import net.kyrptonaught.serverutils.discordBridge.DiscordBridgeMod;
import net.kyrptonaught.serverutils.discordBridge.format.FormatToMC;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BridgeBot extends ListenerAdapter {
    public final MinecraftServer server;
    public final JDA jda;
    public final String webhookUrl;

    public BridgeBot(MinecraftServer server, JDA jda, String webhookUrl) {
        this.server = server;
        this.jda = jda;
        this.jda.addEventListener(this);
        this.webhookUrl = webhookUrl;
        BotCommands.registerCommands(jda);
    }

    private static final String payloadTemplate = " {\"content\":\"%1$s\",\"username\":\"%2$s\",\"avatar_url\":\"%3$s\",\"allowed_mentions\":{%4$s}}";

    public void sendMessage(String name, String url, String msg) {
        sendMessage(name, url, msg, false);
    }

    public void sendMessage(String name, String url, String msg, boolean allowMentions) {
        String test = payloadTemplate.formatted(msg, name, url, allowMentions ? " \"parse\":[\"users\",\"roles\",\"everyone\"]" : "\"parse\": []");
        BackendServerModule.asyncPostAlt(webhookUrl, test);
    }

    public void sendEmbed(String title, String msg, int hexColor) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        if (title != null) embedBuilder.setTitle(title);
        if (hexColor != 0) embedBuilder.setColor(hexColor);
        embedBuilder.setDescription(msg);

        jda.getTextChannelById(getChannel()).sendMessageEmbeds(embedBuilder.build()).queue();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isWebhookMessage() && event.getAuthor().getIdLong() != jda.getSelfUser().getIdLong() && isAllowedChannel(event.getChannel().getIdLong())) {
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
        BotCommands.execute(this, event);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        BotCommands.buttonPressed(this, event);
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        BotCommands.modalInteraction(this, event);
    }

    public void log(String logSource, String message) {
        long channel = DiscordBridgeMod.config().loggingChannelID;
        if (channel != 0) {
            jda.getTextChannelById(DiscordBridgeMod.config().loggingChannelID).sendMessageEmbeds(
                    new EmbedBuilder()
                            .setTitle(logSource)
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
        return DiscordBridgeMod.config().bridgeChannelID;
    }

    public void close() {
        if (jda != null) jda.shutdown();
    }
}
