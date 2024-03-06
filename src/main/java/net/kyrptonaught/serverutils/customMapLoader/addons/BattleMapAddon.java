package net.kyrptonaught.serverutils.customMapLoader.addons;

import net.kyrptonaught.serverutils.customMapLoader.MapSize;
import net.kyrptonaught.serverutils.datapackInteractables.BlockList;

public class BattleMapAddon extends BaseMapAddon {
    public static final String TYPE = "battle_map";

    public MapSizeConfig small_map;
    public MapSizeConfig large_map;
    public MapSizeConfig large_plus_map;
    public MapSizeConfig remastered_map;

    public MapSizeConfig getMapDataForSize(MapSize mapSize) {
        return (switch (mapSize) {
            case AUTO -> null;
            case SMALL -> small_map;
            case LARGE -> large_map;
            case LARGE_PLUS -> large_plus_map;
            case REMASTERED -> remastered_map;
        });
    }

    public boolean hasSize(MapSize size) {
        return getMapDataForSize(size) != null;
    }

    public String getDirectoryInZip(MapSize mapSize) {
        return "world/" + mapSize.fileName + "/";
    }

    public static class MapSizeConfig {
        public String center_coords;
        public String[] center_spawn_coords;
        public String[] random_spawn_coords;
        public String world_border_coords_1;
        public String world_border_coords_2;
        public String[] chest_tracker_coords;
        public BlockList interactable_blocklist;

    }
}
