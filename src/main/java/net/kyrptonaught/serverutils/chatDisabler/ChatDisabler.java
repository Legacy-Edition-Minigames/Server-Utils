package net.kyrptonaught.serverutils.chatDisabler;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.kyrptonaught.serverutils.ModuleWConfig;
import net.kyrptonaught.serverutils.discordBridge.Integrations;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ChatDisabler extends ModuleWConfig<ChatDisablerConfig> {
    public static boolean CHATENABLED = true;

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("chatdisabler")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("enablechat")
                        .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    CHATENABLED = BoolArgumentType.getBool(context, "enabled");
                                    ChatDisablerConfig config = getConfig();

                                    Integrations.chatDisabler(context.getSource().getName(), CHATENABLED, config);
                                    if (CHATENABLED) {
                                        if (config.notifyChatEnabled)
                                            broadcast(context.getSource().getServer(), config.enabledMessage);
                                    } else if (config.notifyChatDisabled)
                                        broadcast(context.getSource().getServer(), config.disabledMessage);
                                    return 1;
                                }))));
    }

    public static void broadcast(MinecraftServer server, String message) {
        server.getPlayerManager().broadcast(Text.literal(message), false);
    }

    @Override
    public ChatDisablerConfig createDefaultConfig() {
        return new ChatDisablerConfig();
    }
}
