package net.kyrptonaught.serverutils.scoreboardsuffix;

import com.google.gson.Gson;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.kyrptonaught.serverutils.Module;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Collections;

public class ScoreboardSuffixMod extends Module {
    public static final String MOD_ID = "scoreboardsuffix";
    public static final Gson GSON = ServerUtilsMod.config.getGSON();

    public static PlayerSuffixStorage playerSuffixStorage;

    public void onInitialize() {
        //String input = "{\"input\" : [\" ❤\", \"scoreboard=lives\", \" ⚔\", \"scoreboard=kills\"]}";
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            playerSuffixStorage = server.getWorld(World.OVERWORLD).getPersistentStateManager().getOrCreate(PlayerSuffixStorage::fromNbt, PlayerSuffixStorage::new, MOD_ID);
        });
    }

    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("setScoreboardSuffix")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("format", StringArgumentType.greedyString())
                        .executes(context -> {
                            String format = StringArgumentType.getString(context, "format");
                            format = "{\"input\" :" + format + "}";
                            playerSuffixStorage.setSuffixFormatInput(format);
                            triggerForceUpdate(context.getSource().getServer());
                            return 1;
                        })));
        dispatcher.register(CommandManager.literal("setSuffixFont")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("fontPlaceholder", StringArgumentType.word())
                        .then(CommandManager.argument("font", StringArgumentType.string())
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .executes(context -> executeSetSuffix(context, EntityArgumentType.getPlayers(context, "player"))))
                                .executes(context -> executeSetSuffix(context, Collections.singleton(context.getSource().getPlayer()))))));
        dispatcher.register(CommandManager.literal("scoreboardSuffixForceUpdate")
                .requires((source) -> source.hasPermissionLevel(2))
                .executes(context -> {
                    triggerForceUpdate(context.getSource().getServer());
                    return 1;
                }));
    }

    public static int executeSetSuffix(CommandContext<ServerCommandSource> context, Collection<ServerPlayerEntity> players) {
        String placeholder = new Identifier(StringArgumentType.getString(context, "fontPlaceholder")).toString();
        String font = new Identifier(StringArgumentType.getString(context, "font")).toString();
        players.forEach(serverPlayerEntity -> {
            String playerName = serverPlayerEntity.getEntityName();
            playerSuffixStorage.setFont(playerName, placeholder, font);
            triggerForceUpdate(context.getSource().getServer());
        });
        return 1;
    }

    public static void triggerForceUpdate(MinecraftServer server) {
        server.getPlayerManager().getPlayerList().forEach(serverPlayerEntity -> {
            PlayerListS2CPacket packet = new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, serverPlayerEntity);
            server.getPlayerManager().sendToAll(packet);
        });
    }
}