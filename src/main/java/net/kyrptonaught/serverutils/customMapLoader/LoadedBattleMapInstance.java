package net.kyrptonaught.serverutils.customMapLoader;

import net.kyrptonaught.serverutils.customMapLoader.addons.BattleMapAddon;
import net.kyrptonaught.serverutils.dimensionLoader.DimensionLoaderMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class LoadedBattleMapInstance {

    private final boolean centralSpawnEnabled;

    private final MapSize selectedMapSize;

    private final BattleMapAddon battleMapAddon;

    private final Identifier dimID;

    public List<String> unusedInitialSpawns;

    public List<String> unusedRandomSpawns;

    private Function<MinecraftServer, Boolean> preTriggerDatapackCondition;
    private Collection<CommandFunction<ServerCommandSource>> datapackFunctions;

    public boolean finishedLoading = false;

    public boolean scheduleToRemove = false;

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
            unusedRandomSpawns = unusedInitialSpawns;
        }
    }

    public String getNextInitialSpawn() {
        return unusedInitialSpawns.remove(getWorld().random.nextInt(unusedInitialSpawns.size()));
    }

    public String getUnusedRandomSpawn() {
        if (unusedRandomSpawns.isEmpty())
            unusedRandomSpawns = new ArrayList<>(Arrays.asList(getSizedAddon().random_spawn_coords));

        return unusedRandomSpawns.remove(getWorld().random.nextInt(unusedRandomSpawns.size()));
    }

    public void setPreTriggerDatapackCondition(Function<MinecraftServer, Boolean> execute) {
        this.preTriggerDatapackCondition = execute;
    }

    public boolean runPreTriggerCondition(MinecraftServer server) {
        if (preTriggerDatapackCondition == null)
            return true;

        if (preTriggerDatapackCondition.apply(server)) {
            preTriggerDatapackCondition = null;
            return true;
        }

        return false;
    }

    public void setDatapackFunctions(Collection<CommandFunction<ServerCommandSource>> functions) {
        this.datapackFunctions = functions;
    }

    public void executeDatapack(MinecraftServer server) {
        if (datapackFunctions != null)
            for (CommandFunction<ServerCommandSource> commandFunction : datapackFunctions) {
                server.getCommandFunctionManager().execute(commandFunction, server.getCommandSource().withLevel(2).withSilent());
            }
        datapackFunctions = null;
        finishedLoading = true;
    }
}
