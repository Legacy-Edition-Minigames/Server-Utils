package net.kyrptonaught.serverutils.scoreboardPlayerInfo;

import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.LiteralText;

public class CustomObjective {
    private final String objName;
    private final String displayName;

    public CustomObjective(String objName, String displayName) {
        this.objName = ServerUtilsMod.MOD_ID + "." + objName;
        this.displayName = displayName;
    }

    public void addToScoreboard(Scoreboard scoreboard) {
        if (!scoreboard.containsObjective(objName))
            scoreboard.addObjective(objName, ScoreboardCriterion.DUMMY, new LiteralText(displayName), ScoreboardCriterion.RenderType.INTEGER);
    }

    public void setScoreboardScore(Scoreboard scoreboard, PlayerEntity player, int score) {
        ScoreboardObjective objective = scoreboard.getObjective(objName);
        scoreboard.getPlayerScore(player.getEntityName(), objective).setScore(score);
    }

    public void resetScore(Scoreboard scoreboard, PlayerEntity player) {
        ScoreboardObjective objective = scoreboard.getObjective(objName);
        scoreboard.getPlayerScore(player.getEntityName(), objective).clearScore();
    }
}
