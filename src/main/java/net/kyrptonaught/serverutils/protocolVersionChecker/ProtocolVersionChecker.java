package net.kyrptonaught.serverutils.protocolVersionChecker;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.network.ClientConnection;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.LiteralText;

import java.util.HashMap;

public class ProtocolVersionChecker {
    public static String MOD_ID = "protocolversionchecker";
    private static final String OBJName = ServerUtilsMod.MOD_ID + ".protocolversion";

    public static HashMap<ClientConnection, Integer> connectionProtocolVersion = new HashMap<>();

    public static void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            Scoreboard scoreboard = server.getScoreboard();
            if (!scoreboard.containsObjective(OBJName))
                scoreboard.addObjective(OBJName, ScoreboardCriterion.DUMMY, new LiteralText("Client Protocol Version"), ScoreboardCriterion.RenderType.INTEGER);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (connectionProtocolVersion.containsKey(handler.connection)) {
                int protocolVersion = connectionProtocolVersion.remove(handler.connection);
                Scoreboard scoreboard = server.getScoreboard();
                ScoreboardObjective objective = scoreboard.getObjective(OBJName);
                scoreboard.getPlayerScore(handler.getPlayer().getEntityName(), objective).setScore(protocolVersion);
            }
        });
    }
}
