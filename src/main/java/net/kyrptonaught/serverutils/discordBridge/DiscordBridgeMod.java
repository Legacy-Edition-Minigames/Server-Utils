package net.kyrptonaught.serverutils.discordBridge;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
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
import net.kyrptonaught.serverutils.ModuleWConfig;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class DiscordBridgeMod extends ModuleWConfig<DiscordBridgeConfig> {
    public static BridgeBot bot;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            buildBot(server, getConfig());
            BotCommands.registerCommands(bot, server);
            MessageSender.sendGameMessage("`Server Started`");

        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            if (bot != null) bot.close();
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> MessageSender.sendGameMessage("`Server Stopped`"));
        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> MessageSender.sendChatMessage(sender, message.getSignedContent().plain()));
        LinkingManager.init();
    }


    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("discordLink").executes(context -> {
            //if (getConfig().isMenuBot) {
            ServerPlayerEntity player = context.getSource().getPlayer();
            String link = LinkingManager.beginLink(player);
            BookGUI.showLinkGUI(player, link);

            context.getSource().sendFeedback(Text.literal("Link started: " + link), false);
            // }
            return 1;
        }));

        dispatcher.register(CommandManager.literal("discordMSG").requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("msg", TextArgumentType.text()).executes(context -> {
                    Text text = TextArgumentType.getTextArgument(context, "msg");
                    MessageSender.sendGameMessage(text);
                    return 1;
                })));
    }

    public static String getUserHeadURL(ServerPlayerEntity player) {
        return DiscordBridgeMod.config().PlayerSkinURL
                .replace("%PLAYERNAME%", player.getGameProfile().getName())
                .replace("%PLAYERUUID%", player.getUuidAsString());
    }

    public void buildBot(MinecraftServer server, DiscordBridgeConfig config) {
        JDA jda = JDABuilder.createDefault(config.BotToken)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .enableIntents(GatewayIntent.GUILD_EMOJIS_AND_STICKERS)
                .enableCache(CacheFlag.EMOJI)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL)
                .setActivity(Activity.playing(config.PlayingStatus))
                .build();

        if (config.isMenuBot) {
            bot = new MenuBot(server, jda, config.channelID);
            return;
        }

        WebhookClient client = new WebhookClientBuilder(config.webhookURL)
                .setWait(false)
                .setDaemon(true)
                .build();

        bot = new BridgeBot(server, jda, client, config.channelID);
    }


    public static DiscordBridgeConfig config() {
        return ServerUtilsMod.DiscordBridgeModule.getConfig();
    }

    @Override
    public DiscordBridgeConfig createDefaultConfig() {
        return new DiscordBridgeConfig();
    }
}
