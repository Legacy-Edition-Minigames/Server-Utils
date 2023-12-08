package net.kyrptonaught.serverutils.welcomeMessage;

import net.kyrptonaught.serverutils.ModuleWConfig;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

public class WelcomeModule extends ModuleWConfig<WelcomeMessageConfig> {

    private static final HashSet<UUID> playerMsgSent = new HashSet<>();

    public static void trySendWelcomeMessage(MinecraftServer server, ServerPlayerEntity player) {
        if (playerMsgSent.contains(player.getUuid()))
            return;
        WelcomeMessageConfig config = ServerUtilsMod.WelcomeMessageModule.getConfig();
        Optional<CommandFunction<ServerCommandSource>> function = server.getCommandFunctionManager().getFunction(new Identifier(config.function));
        function.ifPresent(commandFunction -> server.getCommandFunctionManager().execute(commandFunction, player.getCommandSource()));
        playerMsgSent.add(player.getUuid());
    }


    @Override
    public WelcomeMessageConfig createDefaultConfig() {
        return new WelcomeMessageConfig();
    }
}
