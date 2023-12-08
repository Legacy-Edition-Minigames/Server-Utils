package net.kyrptonaught.serverutils.serverMetadataSpoofer;

import com.mojang.authlib.GameProfile;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ServerMetadataSpoofer {

    public static List<GameProfile> spoofConnectedPlayers(MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();

        List<PlayerData> playerDataList = new ArrayList<>();

        server.getPlayerManager().getPlayerList().forEach(playerEntity -> {
            String playerName = playerEntity.allowsServerListing() ? playerEntity.getNameForScoreboard() : MinecraftServer.ANONYMOUS_PLAYER_PROFILE.getName();
            UUID uuid = playerEntity.allowsServerListing() ? playerEntity.getGameProfile().getId() : MinecraftServer.ANONYMOUS_PLAYER_PROFILE.getId();

            Team playerTeam = scoreboard.getScoreHolderTeam(playerEntity.getNameForScoreboard());
            if (playerTeam == null)
                playerDataList.add(new PlayerData(playerName, uuid, 999));
            else {
                String teamName = playerTeam.getName();
                int num = 998;
                if (teamName.matches("P\\d\\d"))
                    num = Integer.parseInt(teamName.substring(1));

                playerName = "§" + playerTeam.getColor().getCode() + playerName;
                if (num == 1) playerName += " (Host)";
                playerDataList.add(new PlayerData(playerName, uuid, num));
            }
        });

        playerDataList.sort(Comparator.comparing(playerData -> playerData.playerNum));
        return playerDataList.subList(0, Math.min(playerDataList.size(), 16)).stream().map(playerData -> new GameProfile(playerData.uuid, playerData.playerName)).toList();
    }

    public record PlayerData(String playerName, UUID uuid, int playerNum) {
    }
}
