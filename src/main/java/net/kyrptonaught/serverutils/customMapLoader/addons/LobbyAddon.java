package net.kyrptonaught.serverutils.customMapLoader.addons;

import net.kyrptonaught.serverutils.customMapLoader.MapSize;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;

public class LobbyAddon extends BaseAddon {
    public static final String TYPE = "lobby_map";

    public String resource_pack;

    public Identifier dimensionType_id;
    public transient DimensionType loadedDimensionType;

    public String[] spawn_coords;

    public String winner_coords;

    public String getDirectoryInZip(MapSize mapSize) {
        return "world/lobby/";
    }
}
