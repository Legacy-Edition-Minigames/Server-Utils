package net.kyrptonaught.serverutils.discordBridge.bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.kyrptonaught.serverutils.discordBridge.DiscordBridgeMod;
import net.kyrptonaught.serverutils.discordBridge.format.FormatToMC;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collections;

public class BridgeBot extends ListenerAdapter {
    public final MinecraftServer server;
    public final JDA jda;

    public BridgeBot(MinecraftServer server, JDA jda) {
        this.server = server;
        this.jda = jda;
        this.jda.addEventListener(this);
        BotCommands.registerCommands(jda);
    }

    public void sendMessage(String msg, boolean mentions) {
        MessageCreateData message = new MessageCreateBuilder()
                .setContent(msg)
                .setAllowedMentions(mentions ? null : Collections.emptyList())
                .build();

        jda.getTextChannelById(getChannel()).sendMessage(message).queue();
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
        if (event != null && shouldRespondToMessage(event)) {
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

    public void lockChannel(boolean locked) {
        if (locked) {
            jda.getTextChannelById(getChannel()).upsertPermissionOverride(jda.getRoleById(DiscordBridgeMod.config().linkRoleID))
                    .deny(Permission.MESSAGE_SEND).queue();
        } else {
            jda.getTextChannelById(getChannel()).upsertPermissionOverride(jda.getRoleById(DiscordBridgeMod.config().linkRoleID))
                    .grant(Permission.MESSAGE_SEND).queue();
        }
    }

    private boolean shouldRespondToMessage(MessageReceivedEvent event) {
        return (event.getMessage().getType() == MessageType.DEFAULT || event.getMessage().getType() == MessageType.INLINE_REPLY) &&
                !event.isWebhookMessage() &&
                event.getAuthor().getIdLong() != jda.getSelfUser().getIdLong() &&
                (isAllowedChannel(event.getChannel().getIdLong()));
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
