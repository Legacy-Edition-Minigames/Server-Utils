package net.kyrptonaught.serverutils.discordBridge.bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.kyrptonaught.serverutils.discordBridge.DiscordBridgeMod;
import net.kyrptonaught.serverutils.discordBridge.MessageSender;
import net.kyrptonaught.serverutils.discordBridge.linking.LinkingManager;
import net.minecraft.server.MinecraftServer;

import java.util.function.Consumer;

public class MenuBot extends BridgeBot {

    public MenuBot(MinecraftServer server, JDA jda) {
        super(server, jda, null);
        this.jda.updateCommands().addCommands(
                Commands.slash("link", "Link your Minecraft Account with your Discord. Must be initiated from in-game")
                        .setGuildOnly(true)
                        .addOption(OptionType.STRING, "linkid", "The Link ID generated in-game")
        ).queue();
    }

    @Override
    public void registerCommand(String cmd, String description, Consumer<SlashCommandInteraction> execute) {
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (isAllowedChannel(event.getChannel().getIdLong())) {
            if (event.getName().equals("link")) {
                String linkID = event.getOption("linkid", OptionMapping::getAsString);
                String discordID = event.getMember().getId();
                event.deferReply().setEphemeral(true).queue();

                LinkingManager.generateLink(linkID, discordID, event);
            }
        }
    }

    public void close() {
        if (jda != null) jda.shutdown();
    }
}