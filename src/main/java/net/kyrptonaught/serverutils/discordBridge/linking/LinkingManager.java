package net.kyrptonaught.serverutils.discordBridge.linking;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.utils.FileUpload;
import net.kyrptonaught.serverutils.backendServer.BackendServerModule;
import net.kyrptonaught.serverutils.discordBridge.DiscordBridgeMod;
import net.kyrptonaught.serverutils.discordBridge.MessageSender;
import net.kyrptonaught.serverutils.discordBridge.bot.BridgeBot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Collections;

public class LinkingManager {

    public static void prepareChannel(BridgeBot bot, long channel) {
        if (channel == 0 || bot == null) return;
        MessageHistory history = MessageHistory.getHistoryFromBeginning(bot.jda.getTextChannelById(channel)).complete();
        for (Message message : history.getRetrievedHistory()) {
            message.delete().queue();
        }
        generateDiscordInput(bot.jda, channel);
    }

    public static void beginLink(ServerPlayerEntity player) {
        String linkID = RandomStringUtils.randomAlphanumeric(5).toLowerCase();
        BackendServerModule.asyncPost("link/start/" + linkID + "/" + player.getUuidAsString(), (success, response) -> {
            if (success) {
                player.sendMessage(Text.empty().append(Text.literal("[Discord] ").formatted(Formatting.BLUE)).append("Link started: " + linkID));
                LinkingGUI.showLinkGUI(player, linkID);
            }
        });
    }

    public static void generateDiscordInput(JDA jda, long channel) {
        MessageEmbed embed = new EmbedBuilder()
                .setDescription("Click \"Link\" below to link your account")
                .build();

        jda.getTextChannelById(channel).sendMessageEmbeds(Collections.singleton(embed))
                .addActionRow(Button.primary("link:start", "Link"))
                .queue();
    }

    public static void displayLinkInput(ButtonInteractionEvent event) {
        TextInput input = TextInput.create("link:input", "Link Code", TextInputStyle.SHORT)
                .setRequired(true)
                .setMinLength(5)
                .setMaxLength(5)
                .build();

        event.replyModal(Modal.create("link:modal", "Enter your Link code").addActionRow(input).build()).queue();
    }

    public static void linkInputResults(ModalInteractionEvent event) {
        String linkID = event.getValue("link:input").getAsString();
        String discordID = event.getInteraction().getMember().getId();

        BackendServerModule.asyncPost("link/finish/" + linkID + "/" + discordID, (success, response) -> {
            if (!success) {
                event.reply("Error").setEphemeral(true).queue();
                return;
            }
            event.reply("Linked!").setEphemeral(true).queue();
            event.getGuild().addRoleToMember(event.getMember(), event.getGuild().getRoleById(DiscordBridgeMod.config().linkRoleID)).queue();

            event.getMember();
            MessageSender.sendLogMessage("<@" + event.getMember().getId() + "> linked their account to MC -> " + response.body());
        });
    }
}
