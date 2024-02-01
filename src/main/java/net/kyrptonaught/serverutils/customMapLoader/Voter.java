package net.kyrptonaught.serverutils.customMapLoader;

import net.minecraft.scoreboard.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Voter {

    private static final HashMap<String, Set<UUID>> mapVotes = new HashMap<>();
    private static String objName = "lem.voting.mapvotes";

    public static void prepVote(MinecraftServer server, List<LemModConfig> loadedMods) {
        mapVotes.clear();
        for (LemModConfig mod : loadedMods) {
            mapVotes.put(mod.id, new HashSet<>());
        }

        ServerScoreboard scoreboard = server.getScoreboard();

        ScoreboardObjective obj = scoreboard.getNullableObjective(objName);
        if (obj != null) scoreboard.removeObjective(obj);

        obj = scoreboard.addObjective(objName, ScoreboardCriterion.DUMMY, Text.literal("Map Votes"), ScoreboardCriterion.RenderType.INTEGER, true, null);

        scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, obj);
    }

    public static String endVote(MinecraftServer server){
        ServerScoreboard scoreboard = server.getScoreboard();
        scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, null);

        return calculateWinner();
    }

    private static String calculateWinner() {
        int maxVotes = 0;
        List<String> winningMaps = new ArrayList<>();

        for (String map : mapVotes.keySet()) {
            int votes = mapVotes.get(map).size();

            if (votes > maxVotes) {
                maxVotes = votes;
                winningMaps.clear();
            }

            if (votes == maxVotes) {
                winningMaps.add(map);
            }
        }

        Random random = new Random();
        return winningMaps.get(random.nextInt(winningMaps.size()));
    }


    public static void voteFor(MinecraftServer server, ServerPlayerEntity player, String map, Text mapName) {
        if (!mapVotes.containsKey(map)) mapVotes.put(map, new HashSet<>());

        removeVote(server, player);
        mapVotes.get(map).add(player.getUuid());
        updateScore(server, map, mapName);
    }

    public static void removeVote(MinecraftServer server, ServerPlayerEntity player) {
        for (String id : mapVotes.keySet()) {
            if (mapVotes.get(id).remove(player.getUuid())) {
                updateScore(server, id, CustomMapLoaderMod.loadedMaps.get(id).getName());
            }
        }
    }


    private static void updateScore(MinecraftServer server, String map, Text mapName) {
        ScoreHolder holder = new ScoreHolder() {
            @Override
            public String getNameForScoreboard() {
                return map;
            }

            @Nullable
            @Override
            public Text getDisplayName() {
                return mapName;
            }
        };

        ServerScoreboard scoreboard = server.getScoreboard();
        ScoreboardObjective objective = scoreboard.getNullableObjective(objName);

        int votes = mapVotes.get(map).size();
        if (votes > 0) {
            scoreboard.getOrCreateScore(holder, objective).setScore(votes);
        } else {
            scoreboard.removeScore(holder, objective);
        }
    }
}
