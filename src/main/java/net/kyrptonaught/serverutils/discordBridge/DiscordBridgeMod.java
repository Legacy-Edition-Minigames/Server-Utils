package net.kyrptonaught.serverutils.discordBridge;

import com.mojang.brigadier.CommandDispatcher;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyrptonaught.serverutils.ModuleWConfig;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.discordBridge.bot.BridgeBot;
import net.kyrptonaught.serverutils.discordBridge.linking.LinkingManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class DiscordBridgeMod extends ModuleWConfig<DiscordBridgeConfig> {
    public static BridgeBot bot;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            buildBot(server, getConfig());
            MessageSender.sendGameMessage("Server Started", 0xffffff);
            LinkingManager.prepareChannel(bot, getConfig().linkChannelID);
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            MessageSender.sendGameMessage("Server Stopped", 0xffffff);
            if (bot != null) bot.close();
        });
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> MessageSender.sendChatMessage(sender, message.getSignedContent()));

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            Integrations.sendLeaveMessage(Text.translatable("multiplayer.player.left", handler.player.getDisplayName()).formatted(Formatting.YELLOW));
        });
    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        DiscordBridgeCommands.registerCommands(dispatcher);
    }

    public static String getUserHeadURL(ServerPlayerEntity player) {
        return DiscordBridgeMod.config().PlayerSkinURL
                .replace("%PLAYERNAME%", player.getGameProfile().getName())
                .replace("%PLAYERUUID%", player.getUuidAsString());
    }

    public void buildBot(MinecraftServer server, DiscordBridgeConfig config) {
        if (config.BotToken == null)
            return;

        JDA jda = JDABuilder.createDefault(config.BotToken)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .enableIntents(GatewayIntent.GUILD_EMOJIS_AND_STICKERS)
                .enableCache(CacheFlag.EMOJI)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setActivity(Activity.playing(config.PlayingStatus))
                .build();

        try {
            bot = new BridgeBot(server, jda.awaitReady());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static DiscordBridgeConfig config() {
        return ServerUtilsMod.DiscordBridgeModule.getConfig();
    }

    @Override
    public DiscordBridgeConfig createDefaultConfig() {
        return new DiscordBridgeConfig();
    }
}
