package net.kyrptonaught.serverutils.customMapLoader.voting;

import net.kyrptonaught.serverutils.customMapLoader.CustomMapLoaderMod;
import net.kyrptonaught.serverutils.customMapLoader.MapSize;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

public class HostOptions {
    public static MapSize selectedMapSize = MapSize.AUTO;

    public static void enableDisableMap(MinecraftServer server, Identifier map, boolean enabled) {
        CustomMapLoaderMod.BATTLE_MAPS.get(map).isAddonEnabled = enabled;
        Voter.updateScore(server, map);
    }
}
