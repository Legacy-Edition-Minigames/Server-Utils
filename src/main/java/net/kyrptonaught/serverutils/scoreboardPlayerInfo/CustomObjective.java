package net.kyrptonaught.serverutils.scoreboardPlayerInfo;

import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;
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
        if (scoreboard.getNullableObjective(objName) == null)
            scoreboard.addObjective(objName, ScoreboardCriterion.DUMMY, Text.literal(displayName), ScoreboardCriterion.RenderType.INTEGER, false, null);
    }

    public void addPlayerScoresToScoreboard(Scoreboard scoreboard) {
        ScoreboardObjective objective = scoreboard.getNullableObjective(objName);
        for (String player : savedPlayerValues.keySet()) {
            scoreboard.getOrCreateScore(ScoreHolder.fromName(player), objective).setScore(savedPlayerValues.get(player));
        }
    }


    public void addPlayerScoresToScoreboard(Scoreboard scoreboard, Collection<ServerPlayerEntity> players) {
        ScoreboardObjective objective = scoreboard.getNullableObjective(objName);
        for (ServerPlayerEntity player : players) {
            if (savedPlayerValues.containsKey(player.getNameForScoreboard()))
                scoreboard.getOrCreateScore(ScoreHolder.fromName(player.getNameForScoreboard()), objective).setScore(savedPlayerValues.get(player.getNameForScoreboard()));
        }
    }

    public void setScore(PlayerEntity player, int score) {
        savedPlayerValues.put(player.getNameForScoreboard(), score);
    }

    public void resetScore(Scoreboard scoreboard, PlayerEntity player) {
        setScore(player, 0);
        ScoreboardObjective objective = scoreboard.getNullableObjective(objName);
        scoreboard.getOrCreateScore(ScoreHolder.fromName(player.getNameForScoreboard()), objective).setScore(0);
    }
}
