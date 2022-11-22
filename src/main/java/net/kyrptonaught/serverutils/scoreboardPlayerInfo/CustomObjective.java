package net.kyrptonaught.serverutils.scoreboardPlayerInfo;

import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class CustomObjective {
    private final String objName;
    private final String displayName;

    private final HashMap<String, Integer> savedPlayerValues = new HashMap<>();

    public CustomObjective(String objName, String displayName) {
        this.objName = ServerUtilsMod.MOD_ID + "." + objName;
        this.displayName = displayName;
    }

    public void addToScoreboard(Scoreboard scoreboard) {
        if (!scoreboard.containsObjective(objName))
            scoreboard.addObjective(objName, ScoreboardCriterion.DUMMY, Text.literal(displayName), ScoreboardCriterion.RenderType.INTEGER);
    }

    public void addPlayerScoresToScoreboard(Scoreboard scoreboard) {
        ScoreboardObjective objective = scoreboard.getObjective(objName);
        for (String player : savedPlayerValues.keySet()) {
            scoreboard.getPlayerScore(player, objective).setScore(savedPlayerValues.get(player));
        }
    }


    public void addPlayerScoresToScoreboard(Scoreboard scoreboard, Collection<ServerPlayerEntity> players) {
        ScoreboardObjective objective = scoreboard.getObjective(objName);
        for (ServerPlayerEntity player : players) {
            if (savedPlayerValues.containsKey(player.getEntityName()))
                scoreboard.getPlayerScore(player.getEntityName(), objective).setScore(savedPlayerValues.get(player.getEntityName()));
        }
    }

    public void setScore(PlayerEntity player, int score) {
        savedPlayerValues.put(player.getEntityName(), score);
    }

    public void resetScore(Scoreboard scoreboard, PlayerEntity player) {
        setScore(player, 0);
        ScoreboardObjective objective = scoreboard.getObjective(objName);
        scoreboard.getPlayerScore(player.getEntityName(), objective).setScore(0);
    }
}
