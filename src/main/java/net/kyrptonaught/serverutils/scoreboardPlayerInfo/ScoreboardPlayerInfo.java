package net.kyrptonaught.serverutils.scoreboardPlayerInfo;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.util.HashMap;

public class ScoreboardPlayerInfo {
    public static String MOD_ID = "scoreboardplayerinfo";

    private static final CustomObjective protocolObjective = new CustomObjective("mcprotocolversion", "Client MC Protocol Version");
    private static final CustomObjective hasOptifineObjective = new CustomObjective("hasoptifine", "Client has Optifine");
    private static final CustomObjective hasLEMClientObjective = new CustomObjective("haslemclient", "Client has LEMClientHelper");

    private final static HashMap<ClientConnection, Integer> connectionProtocolVersion = new HashMap<>();

    public static void onInitialize() {
        ScoreboardPlayerInfoNetworking.registerReceivePacket();

        ServerLifecycleEvents.SERVER_STARTED.register(ScoreboardPlayerInfo::registerScoreboardOBJs);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerConnect(server, handler));
    }

    public static void registerScoreboardOBJs(MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();
        ScoreboardObjective[] objectives = scoreboard.getObjectives().toArray(ScoreboardObjective[]::new);
        for (int i = objectives.length - 1; i >= 0; i--) {
            if (objectives[i].getName().startsWith(ServerUtilsMod.MOD_ID + "."))
                scoreboard.removeObjective(objectives[i]);
        }

        protocolObjective.addToScoreboard(scoreboard);
        hasOptifineObjective.addToScoreboard(scoreboard);
        hasLEMClientObjective.addToScoreboard(scoreboard);
    }

    public static void onPlayerConnect(MinecraftServer server, ServerPlayNetworkHandler handler) {
        if (connectionProtocolVersion.containsKey(handler.connection)) {
            int protocolVersion = connectionProtocolVersion.remove(handler.connection);
            protocolObjective.setScoreboardScore(server.getScoreboard(), handler.player, protocolVersion);
        }
    }

    public static void addClientConnectionProtocol(ClientConnection connection, int protocol) {
        connectionProtocolVersion.put(connection, protocol);
    }

    public static void setHasLEMClient(MinecraftServer server, PlayerEntity player, boolean hasLEMClient) {
        hasLEMClientObjective.setScoreboardScore(server.getScoreboard(), player, hasLEMClient ? 1 : 0);
    }

    public static void setHasOptifine(MinecraftServer server, PlayerEntity player, boolean hasOptifine) {
        hasOptifineObjective.setScoreboardScore(server.getScoreboard(), player, hasOptifine ? 1 : 0);
    }
}
