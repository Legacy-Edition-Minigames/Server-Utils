package net.kyrptonaught.serverutils.dimensionLoader;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.kyrptonaught.serverutils.FileHelper;
import net.kyrptonaught.serverutils.Module;
import net.kyrptonaught.serverutils.customWorldBorder.CustomWorldBorderMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
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

public class DimensionLoaderMod extends Module {
    public static final HashMap<Identifier, CustomDimHolder> loadedWorlds = new HashMap<>();

    @Override
    public void onInitialize() {
        ServerTickEvents.START_SERVER_TICK.register(DimensionLoaderMod::serverTickWorldAdd);
    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        DimensionLoaderCommand.registerCommands(dispatcher);
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

    public static Text whereAmI(ServerPlayerEntity player) {
        Identifier dimID = player.getWorld().getRegistryKey().getValue();
        if (loadedWorlds.containsKey(dimID))
            dimID = loadedWorlds.get(dimID).copyFromID;
        return Text.translatable("key.world." + dimID.toTranslationKey());
    }

    public static RegistryKey<World> tryGetWorldKey(ServerWorld world) {
        RegistryKey<World> ogKey = world.getRegistryKey();
        if (loadedWorlds.containsKey(ogKey.getValue()))
            return RegistryKey.of(Registry.WORLD_KEY, loadedWorlds.get(ogKey.getValue()).copyFromID);
        return ogKey;
    }

    public static RegistryKey<DimensionType> tryGetDimKey(ServerWorld world) {
        RegistryKey<World> ogKey = world.getRegistryKey();
        if (loadedWorlds.containsKey(ogKey.getValue()))
            return RegistryKey.of(Registry.DIMENSION_TYPE_KEY, loadedWorlds.get(ogKey.getValue()).copyFromID);
        return world.getDimensionKey();
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
                    CustomWorldBorderMod.onDimensionUnload(holder.world.asWorld());
                    it.remove();
                }
            } else if (!holder.wasRegistered()) {
                //I don't really understand how mc registry key/entry shit works, but this does somehow work
                Registry<DimensionType> registry = server.getRegistryManager().get(Registry.DIMENSION_TYPE_KEY);
                RegistryEntry<DimensionType> entry = registry.getEntry(registry.getKey(registry.get(holder.copyFromID)).get()).get();

                RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
                        .setDimensionType(entry)
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


