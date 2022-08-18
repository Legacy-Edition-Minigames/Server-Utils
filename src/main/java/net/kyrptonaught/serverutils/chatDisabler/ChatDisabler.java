package net.kyrptonaught.serverutils.chatDisabler;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.network.MessageType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;

public class ChatDisabler {
    public static String MOD_ID = "chatdisabler";

    public static boolean CHATENABLED = true;

    public static void onInitialize() {
        ServerUtilsMod.configManager.registerFile(MOD_ID, new ChatDisablerConfig());
        CommandRegistrationCallback.EVENT.register(ChatDisabler::registerCommand);
    }

    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher, boolean b) {
        dispatcher.register(CommandManager.literal("chatdisabler")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("enablechat")
                        .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                .executes(context -> {
                                    CHATENABLED = BoolArgumentType.getBool(context, "enabled");

                                    ChatDisablerConfig config = getConfig();
                                    if (CHATENABLED) {
                                        if (config.notifyChatEnabled)
                                            broadcast(context.getSource().getServer(), config.enabledMessage);
                                    } else if (config.notifyChatDisabled) {
                                        broadcast(context.getSource().getServer(), config.disabledMessage);
                                    }
                                    return 1;
                                }))));
    }

    public static void broadcast(MinecraftServer server, String message) {
        server.getPlayerManager().broadcast(new LiteralText(message), MessageType.SYSTEM, Util.NIL_UUID);
    }

    public static ChatDisablerConfig getConfig() {
        return (ChatDisablerConfig) ServerUtilsMod.configManager.getConfig(MOD_ID);
    }
}
