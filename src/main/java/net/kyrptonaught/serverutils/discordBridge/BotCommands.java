package net.kyrptonaught.serverutils.discordBridge;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;

public class BotCommands {


    public static void registerCommands(BridgeBot bot, MinecraftServer server) {
        if (bot == null) return;
        bot.registerCommand("info", "Get the server info", event -> event.reply(getInfoCommandMessage(server)).queue());
    }

    private static String getInfoCommandMessage(MinecraftServer server) {
        StringBuilder message = new StringBuilder();
        message.append("```\n");
        message.append("=============== ").append("Server Status").append(" ===============\n\n");

        PlayerManager players = server.getPlayerManager();
        message.append("Online Players (").append(players.getCurrentPlayerCount()).append("/").append(players.getMaxPlayerCount()).append(")\n");
        for (ServerPlayerEntity player : players.getPlayerList())
            message.append("[").append(player.pingMilliseconds).append("ms] ").append(player.getEntityName()).append("\n");
        message.append("\n");

        double serverTickTime = MathHelper.average(server.lastTickLengths) * 1.0E-6D;
        message.append("Server TPS: ").append(String.format("%.2f", Math.min(1000.0 / serverTickTime, 20))).append("\n\n");
        message.append("Server MSPT: ").append(String.format("%.2f", serverTickTime)).append("\n\n");
        message.append("Server RAM: ").append((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024).append("MB / ").append(Runtime.getRuntime().totalMemory() / 1024 / 1024).append("MB");
        message.append("\n```");
        return message.toString();
    }
}
