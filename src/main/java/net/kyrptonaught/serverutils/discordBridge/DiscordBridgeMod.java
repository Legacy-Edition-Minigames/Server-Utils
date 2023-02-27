package net.kyrptonaught.serverutils.discordBridge;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.kyrptonaught.serverutils.ModuleWConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class DiscordBridgeMod extends ModuleWConfig<DiscordBridgeConfig> {
    public static BridgeBot bot;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            buildBot(server, getConfig());
            BotCommands.registerCommands(bot, server);
            sendGameMessage("Server Started");
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            if (bot != null) bot.close();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> sendGameMessage("Server Stopped"));
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            sendChatMessage(sender, message.getSignedContent().plain());
        });

        ServerMessageEvents.GAME_MESSAGE.register((server, message, overlay) -> sendGameMessage(message.getString()));
    }

    public void sendChatMessage(ServerPlayerEntity player, String message) {
        if (bot != null)
            bot.sendMessage(player.getEntityName(), getUserHeadURL(player), message);
    }

    public void sendGameMessage(String message) {
        if (bot != null)
            bot.sendMessage(getConfig().GameMessageName, getConfig().GameMessageAvatarURL, message);
    }

    public static void sendToAll(MinecraftServer server, Text message) {
        server.sendMessage(message);
        for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
            serverPlayerEntity.sendMessage(message, false);
        }
    }

    public String getUserHeadURL(ServerPlayerEntity player) {
        return getConfig().PlayerSkinURL
                .replace("%PLAYERNAME%", player.getGameProfile().getName())
                .replace("%PLAYERUUID%", player.getUuidAsString());
    }

    public void buildBot(MinecraftServer server, DiscordBridgeConfig config) {
        JDA jda = JDABuilder.createLight(config.BotToken)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setActivity(Activity.playing(config.PlayingStatus))
                .build();

        WebhookClient client = new WebhookClientBuilder(config.webhookURL)
                .setWait(false)
                .setDaemon(true)
                .build();

        bot = new BridgeBot(server, jda, client, config.channelID);
    }

    @Override
    public DiscordBridgeConfig createDefaultConfig() {
        return new DiscordBridgeConfig();
    }
}
