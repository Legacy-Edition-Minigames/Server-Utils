package net.kyrptonaught.serverutils.scoreboardPlayerInfo;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.serverutils.Module;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;

public class ScoreboardPlayerInfo extends Module {
    public static String MOD_ID = "scoreboardplayerinfo";

    private static final CustomObjective protocolObjective = new CustomObjective("mcprotocolversion", "Client MC Protocol Version");
    private static final CustomObjective hasOptifineObjective = new CustomObjective("hasoptifine", "Client has Optifine");
    private static final CustomObjective hasLEMClientObjective = new CustomObjective("haslemclient", "Client has LEMClientHelper");
    private static final CustomObjective hasControllerModObjective = new CustomObjective("hascontroller", "Client has Controller Mod");
    private static final CustomObjective fabricClientObjective = new CustomObjective("fabricclient", "Client is using Fabric");
    private static final CustomObjective forgeClientObjective = new CustomObjective("forgeclient", "Client is using Forge");

    private final static HashMap<ClientConnection, Integer> connectionProtocolVersion = new HashMap<>();

    public void onInitialize() {
        ScoreboardPlayerInfoNetworking.registerReceivePacket();

        ServerLifecycleEvents.SERVER_STARTED.register(ScoreboardPlayerInfo::registerScoreboardOBJs);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerConnect(server, handler));
    }

    public static void registerScoreboardOBJs(MinecraftServer server) {
        ServerScoreboard scoreboard = server.getScoreboard();
        ScoreboardObjective[] objectives = scoreboard.getObjectives().toArray(ScoreboardObjective[]::new);
        for (int i = objectives.length - 1; i >= 0; i--) {
            if (objectives[i].getName().startsWith(ServerUtilsMod.MOD_ID + "."))
                scoreboard.removeObjective(objectives[i]);
        }

        protocolObjective.addToScoreboard(scoreboard);
        hasOptifineObjective.addToScoreboard(scoreboard);
        hasLEMClientObjective.addToScoreboard(scoreboard);
        fabricClientObjective.addToScoreboard(scoreboard);
        forgeClientObjective.addToScoreboard(scoreboard);
    }

    public static void onPlayerConnect(MinecraftServer server, ServerPlayNetworkHandler handler) {
        Scoreboard scoreboard = server.getScoreboard();
        protocolObjective.resetScore(scoreboard, handler.player);
        hasOptifineObjective.resetScore(scoreboard, handler.player);
        hasLEMClientObjective.resetScore(scoreboard, handler.player);
        fabricClientObjective.resetScore(scoreboard, handler.player);
        forgeClientObjective.resetScore(scoreboard, handler.player);

        if (connectionProtocolVersion.containsKey(handler.connection)) {
            int protocolVersion = connectionProtocolVersion.remove(handler.connection);
            protocolObjective.setScoreboardScore(server.getScoreboard(), handler.player, protocolVersion);
        }

        if (ServerPlayNetworking.canSend(handler, new Identifier("fabric:registry/sync")))
            setFabricClient(server, handler.player, true);

    }

    public static void checkBrand(MinecraftServer server, ServerPlayerEntity player, String brand) {
        if (brand.contains("forge"))
            setForgeClient(server, player, true);
    }

    public static void addClientConnectionProtocol(ClientConnection connection, int protocol) {
        connectionProtocolVersion.put(connection, protocol);
    }

    public static void setHasLEMClient(MinecraftServer server, PlayerEntity player, boolean hasLEMClient) {
        hasLEMClientObjective.setScoreboardScore(server.getScoreboard(), player, hasLEMClient ? 2 : 1);
    }

    public static void setHasOptifine(MinecraftServer server, PlayerEntity player, boolean hasOptifine) {
        hasOptifineObjective.setScoreboardScore(server.getScoreboard(), player, hasOptifine ? 2 : 1);
    }

    public static void setHasControllerMod(MinecraftServer server, PlayerEntity player, boolean hasController) {
        hasControllerModObjective.setScoreboardScore(server.getScoreboard(), player, hasController ? 2 : 1);
    }

    public static void setFabricClient(MinecraftServer server, PlayerEntity player, boolean fabricClient) {
        fabricClientObjective.setScoreboardScore(server.getScoreboard(), player, fabricClient ? 2 : 1);
    }

    public static void setForgeClient(MinecraftServer server, PlayerEntity player, boolean forgeClient) {
        forgeClientObjective.setScoreboardScore(server.getScoreboard(), player, forgeClient ? 2 : 0);
    }
}
