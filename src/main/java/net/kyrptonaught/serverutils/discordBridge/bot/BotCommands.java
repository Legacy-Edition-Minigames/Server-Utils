package net.kyrptonaught.serverutils.discordBridge.bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.kyrptonaught.serverutils.discordBridge.DiscordBridgeMod;
import net.kyrptonaught.serverutils.discordBridge.linking.LinkingManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

public class BotCommands {

    public static void registerCommands(JDA jda) {
        jda.updateCommands().addCommands(
                Commands.slash("info", "Get the server info").setGuildOnly(true)
                ).queue();
    }

    public static void execute(BridgeBot bot, SlashCommandInteraction event) {
        switch (event.getName()) {
            case "info" -> BotCommands.infoCommandExecute(bot.server, event);
        }
    }

    public static void buttonPressed(BridgeBot bot, @NotNull ButtonInteractionEvent event) {
        switch (event.getButton().getId()) {
            case "link:start" -> LinkingManager.displayLinkInput(event);
        }
    }

    public static void modalInteraction(BridgeBot bot, ModalInteractionEvent event) {
        switch (event.getModalId()) {
            case "link:modal" -> LinkingManager.linkInputResults(event);
        }
    }

    public static void infoCommandExecute(MinecraftServer server, SlashCommandInteraction event) {
        if (event.getChannel().getIdLong() != DiscordBridgeMod.config().bridgeChannelID) return;

        double serverTickTime = MathHelper.average(server.lastTickLengths) * 1.0E-6D;
        long freeRam = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024;

        StringBuilder playerString = new StringBuilder();
        PlayerManager players = server.getPlayerManager();
        playerString.append("```ts\n").append("Online Players (").append(players.getCurrentPlayerCount()).append("/").append(players.getMaxPlayerCount()).append(")\n");
        for (ServerPlayerEntity player : players.getPlayerList())
            playerString.append("[").append(player.pingMilliseconds).append("ms] ").append(player.getEntityName()).append("\n");
        playerString.append("```");

        event.replyEmbeds(
                new EmbedBuilder()
                        .setTitle("Server Status")
                        .setDescription(playerString.toString())
                        .setColor(0x00aaff)
                        .addField("TPS", String.format("%.2f", Math.min(1000.0 / serverTickTime, 20)), true)
                        .addField("MSPT", String.format("%.2f", serverTickTime), true)
                        .addField("RAM", freeRam + "MB / " + Runtime.getRuntime().totalMemory() / 1024 / 1024 + "MB", true)
                        .build()).queue();
    }

}
