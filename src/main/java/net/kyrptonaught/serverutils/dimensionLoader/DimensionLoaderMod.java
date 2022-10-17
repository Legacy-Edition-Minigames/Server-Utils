package net.kyrptonaught.serverutils.dimensionLoader;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.kyrptonaught.serverutils.FileHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelStorage;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.mixin.MinecraftServerAccess;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DimensionLoaderMod {
    public static String MOD_ID = "dimensionloader";

    public static final HashMap<Identifier, CustomDimHolder> loadedWorlds = new HashMap<>();

    public static void onInitialize() {
        ServerTickEvents.START_SERVER_TICK.register(DimensionLoaderMod::serverTickWorldAdd);
        CommandRegistrationCallback.EVENT.register(DimensionLoaderCommand::registerCommand);
    }

    public static Text loadDimension(MinecraftServer server, Identifier id, Identifier dimID, Collection<CommandFunction> functions) {
        if (loadedWorlds.containsKey(id)) {
            return Text.literal("Dim already registered");
        }

        DimensionType dimensionType = server.getRegistryManager().get(Registry.DIMENSION_TYPE_KEY).get(dimID);
        if (dimensionType == null) {
            return Text.literal("No Dimension Type found");
        }

        if (!backupArenaMap(server, id, dimID)) {
            return Text.literal("Failed creating temp directory");
        }

        loadedWorlds.put(id, new CustomDimHolder(id, dimID, functions));
        return Text.literal("Preparing Dimension");
    }

    public static Text unLoadDimension(MinecraftServer server, Identifier id, Collection<CommandFunction> functions) {
        CustomDimHolder holder = loadedWorlds.get(id);
        if (holder == null)
            return Text.literal("Dimension not found");

        holder.setFunctions(functions);
        holder.scheduleToDelete();
        return Text.literal("Unloading Dimension");
    }

    public static void serverTickWorldAdd(MinecraftServer server) {
        Fantasy fantasy = Fantasy.get(server);
        Iterator<Map.Entry<Identifier, CustomDimHolder>> it = loadedWorlds.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Identifier, CustomDimHolder> pair = it.next();
            CustomDimHolder holder = pair.getValue();

            if (holder.scheduledDelete()) {
                if (holder.deleteFinished(fantasy)) {
                    holder.executeFunctions(server);
                    it.remove();
                }
            } else if (!holder.wasRegistered()) {
                DimensionType dimensionType = server.getRegistryManager().get(Registry.DIMENSION_TYPE_KEY).get(holder.copyFromID);
                RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
                        .setDimensionType(dimensionType)
                        .setMirrorOverworldGameRules(true)
                        .setGenerator(new VoidChunkGenerator(server.getRegistryManager().get(Registry.BIOME_KEY).entryOf(BiomeKeys.PLAINS)));

                holder.register(fantasy.openTemporaryWorld(holder.dimID, worldConfig));
                holder.executeFunctions(server);
            }
        }
    }

    public static boolean backupArenaMap(MinecraftServer server, Identifier id, Identifier newWorld) {
        LevelStorage.Session session = ((MinecraftServerAccess) server).getSession();

        Path worldDir = session.getDirectory(WorldSavePath.ROOT);
        Path arenaDir = getWorldDir(worldDir, id);
        Path newArenaDir = getWorldDir(worldDir, newWorld);

        if (!FileHelper.deleteDir(arenaDir)) return false;
        if (!FileHelper.createDir(arenaDir)) return false;
        return FileHelper.copyDirectory(newArenaDir, arenaDir);
    }

    private static Path getWorldDir(Path worldDirectory, Identifier world) {
        return worldDirectory.resolve("dimensions").resolve(world.getNamespace()).resolve(world.getPath());
    }
}


