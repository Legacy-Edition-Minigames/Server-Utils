package net.kyrptonaught.serverutils.customMapLoader;

import net.kyrptonaught.serverutils.customMapLoader.addons.BattleMapAddon;
import net.kyrptonaught.serverutils.dimensionLoader.DimensionLoaderMod;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public class LoadedBattleMapInstance {

    private final boolean centralSpawnEnabled;

    private final MapSize selectedMapSize;

    private final BattleMapAddon battleMapAddon;

    private final Identifier dimID;

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
}
