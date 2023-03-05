package net.kyrptonaught.serverutils.discordBridge.bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

public class BotCommands {

    public static void registerCommands(BridgeBot bot, MinecraftServer server) {
        if (bot == null) return;
        bot.registerCommand("info", "Get the server info", event -> infoCommandExecute(server, event));
    }

    private static void infoCommandExecute(MinecraftServer server, SlashCommandInteraction event) {
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
