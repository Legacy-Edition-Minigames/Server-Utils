package net.kyrptonaught.serverutils.customMapLoader.voting;

import net.kyrptonaught.serverutils.customMapLoader.CustomMapLoaderMod;
import net.kyrptonaught.serverutils.customMapLoader.MapSize;
import net.kyrptonaught.serverutils.userConfig.UserConfigStorage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class HostOptions {
    public static MapSize selectedMapSize = MapSize.AUTO;

    public static void enableDisableMap(MinecraftServer server, Identifier map, boolean enabled) {
        CustomMapLoaderMod.BATTLE_MAPS.get(map).isAddonEnabled = enabled;
        Voter.updateScore(server, map);
    }

    public static Identifier getMapResourcePackKey(Identifier mapID, Identifier packID) {
        return new Identifier("acceptedpacks", mapID.toUnderscoreSeparatedString() + "." + packID.toUnderscoreSeparatedString());
    }

    public static boolean getMapResourcePackValue(ServerPlayerEntity player, Identifier mapID, Identifier packID) {
        return Boolean.parseBoolean(UserConfigStorage.getValue(player, getMapResourcePackKey(mapID, packID)));
    }

    public static Identifier getGlobalAcceptKey() {
        return new Identifier("acceptedpacks", "global_accept");
    }

    public static boolean getGlobalAcceptValue(ServerPlayerEntity player) {
        return Boolean.parseBoolean(UserConfigStorage.getValue(player, getGlobalAcceptKey()));
    }

    public static Identifier getOverwriteKey(Identifier mapID) {
        return new Identifier("acceptedpacks", "acceptance_policy." + mapID.toUnderscoreSeparatedString() + ".dontaskagain");
    }

    public static boolean getOverwriteValue(ServerPlayerEntity player, Identifier mapID) {
        return Boolean.parseBoolean(UserConfigStorage.getValue(player, getOverwriteKey(mapID)));
    }
}
