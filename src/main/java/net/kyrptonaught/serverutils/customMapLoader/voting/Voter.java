package net.kyrptonaught.serverutils.customMapLoader.voting;

import net.kyrptonaught.serverutils.customMapLoader.CustomMapLoaderMod;
import net.kyrptonaught.serverutils.customMapLoader.addons.BattleMapAddon;
import net.minecraft.scoreboard.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Voter {

    private static final HashMap<Identifier, Set<UUID>> mapVotes = new HashMap<>();
    private static final String objName = "lem.voting.mapvotes";

    public static void prepVote(MinecraftServer server, List<BattleMapAddon> loadedMods) {
        mapVotes.clear();
        for (BattleMapAddon config : loadedMods) {
            mapVotes.put(config.addon_id, new HashSet<>());
        }

        ServerScoreboard scoreboard = server.getScoreboard();

        ScoreboardObjective obj = scoreboard.getNullableObjective(objName);
        if (obj != null) scoreboard.removeObjective(obj);

        obj = scoreboard.addObjective(objName, ScoreboardCriterion.DUMMY, Text.literal("Map Votes"), ScoreboardCriterion.RenderType.INTEGER, true, null);

        scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, obj);
    }

    public static Identifier endVote(MinecraftServer server) {
        ServerScoreboard scoreboard = server.getScoreboard();
        scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, null);

        return calculateWinner();
    }

    private static Identifier calculateWinner() {
        int maxVotes = 0;
        List<Identifier> winningMaps = new ArrayList<>();

        for (Identifier map : mapVotes.keySet()) {
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

    public static void voteFor(MinecraftServer server, ServerPlayerEntity player, Identifier map) {
        if (!mapVotes.containsKey(map)) mapVotes.put(map, new HashSet<>());

        removeVote(server, player);
        mapVotes.get(map).add(player.getUuid());
        updateScore(server, map);
    }

    public static void removeVote(MinecraftServer server, ServerPlayerEntity player) {
        for (Identifier id : mapVotes.keySet()) {
            if (mapVotes.get(id).remove(player.getUuid())) {
                updateScore(server, id);
            }
        }
    }

    public static void updateScore(MinecraftServer server, Identifier map) {
        ScoreHolder holder = new ScoreHolder() {
            @Override
            public String getNameForScoreboard() {
                return map.toString();
            }

            @Nullable
            @Override
            public Text getDisplayName() {
                return CustomMapLoaderMod.BATTLE_MAPS.get(map).getNameText();
            }
        };

        if (!CustomMapLoaderMod.BATTLE_MAPS.get(map).isAddonEnabled)
            mapVotes.remove(map);

        ServerScoreboard scoreboard = server.getScoreboard();
        ScoreboardObjective objective = scoreboard.getNullableObjective(objName);

        Set<UUID> votes = mapVotes.get(map);
        if (votes != null && !votes.isEmpty()) {
            scoreboard.getOrCreateScore(holder, objective).setScore(votes.size());
        } else {
            scoreboard.removeScore(holder, objective);
        }
    }
}
