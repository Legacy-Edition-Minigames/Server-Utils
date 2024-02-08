package net.kyrptonaught.serverutils.customMapLoader;

import net.kyrptonaught.serverutils.customMapLoader.addons.BattleMapAddon;
import net.kyrptonaught.serverutils.dimensionLoader.DimensionLoaderMod;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LoadedBattleMapInstance {

    private final boolean centralSpawnEnabled;

    private final MapSize selectedMapSize;

    private final BattleMapAddon battleMapAddon;

    private final Identifier dimID;

    public List<String> unusedInitialSpawns;

    public LoadedBattleMapInstance(boolean centralSpawnEnabled, MapSize selectedMapSize, BattleMapAddon battleMapAddon, Identifier dimID) {
        this.centralSpawnEnabled = centralSpawnEnabled;
        this.selectedMapSize = selectedMapSize;
        this.battleMapAddon = battleMapAddon;
        this.dimID = dimID;
    }

    public BattleMapAddon getAddon() {
        return battleMapAddon;
    }

    public BattleMapAddon.MapSizeConfig getSizedAddon() {
        return battleMapAddon.getMapDataForSize(selectedMapSize);
    }

    public boolean isCentralSpawnEnabled() {
        return centralSpawnEnabled;
    }

    public Identifier getDimID() {
        return dimID;
    }

    public ServerWorld getWorld() {
        return DimensionLoaderMod.loadedWorlds.get(dimID).world.asWorld();
    }

    public void setInitialSpawns(boolean central) {
        if (central) {
            unusedInitialSpawns = new ArrayList<>(Arrays.asList(getSizedAddon().center_spawn_coords));
        } else {
            unusedInitialSpawns = new ArrayList<>(Arrays.asList(getSizedAddon().random_spawn_coords));
        }
    }

    public String getNextInitialSpawn(){
        return unusedInitialSpawns.remove(getWorld().random.nextInt(unusedInitialSpawns.size()));
    }

}
