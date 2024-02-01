package net.kyrptonaught.serverutils.customMapLoader;

import com.google.gson.JsonElement;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.kyrptonaught.serverutils.FileHelper;
import net.kyrptonaught.serverutils.Module;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.dimensionLoader.CustomDimHolder;
import net.kyrptonaught.serverutils.dimensionLoader.DimensionLoaderMod;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.stream.Stream;

public class CustomMapLoaderMod extends Module {

    public static final HashMap<String, LemModConfig> loadedMaps = new HashMap<>();

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(this::discoverLemmods);
    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        CustomMapLoaderCommands.registerCommands(dispatcher);
    }

    public static void prepareLemmod(MinecraftServer server, Identifier dimID) {
        String winner = Voter.endVote(server);

        LemModConfig config = CustomMapLoaderMod.loadedMaps.get(winner);
        MapSize mapSize = CustomMapLoaderMod.autoSelectMapSize(config, server.getCurrentPlayerCount());
        Vec3d pos = config.getCoordsForSize(mapSize);

        Path path = server.getSavePath(WorldSavePath.ROOT).resolve("dimensions").resolve(dimID.getNamespace()).resolve(dimID.getPath());
        CustomMapLoaderMod.unZipMap(path, config, mapSize);

        DimensionLoaderMod.loadDimension(dimID, config.dimensionType, server2 -> {
            CustomDimHolder holder = DimensionLoaderMod.loadedWorlds.get(dimID);

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ServerUtilsMod.SwitchableResourcepacksModule.execute(config.pack, Collections.singleton(player));
                player.teleport(holder.world.asWorld(), pos.x, pos.y, pos.z, player.getYaw(), player.getPitch());
            }

        });
    }

    public void discoverLemmods(MinecraftServer server) {
        long time = System.nanoTime();
        Path dir = FabricLoader.getInstance().getGameDir().resolve("lebmods");
        try (Stream<Path> files = Files.walk(dir, 2)) {
            files.forEach(path -> {
                try {
                    if (!Files.isDirectory(path) && path.getFileName().toString().endsWith(".lebmod")) {
                        String configJson = FileHelper.readFileFromZip(path, "config.json");

                        LemModConfig config = ServerUtilsMod.getGson().fromJson(configJson, LemModConfig.class);

                        if (path.getParent().getFileName().toString().equals("base")) {
                            config.isBaseMap = true;
                            config.mappack = "base_" + config.mappack;
                        } else if (!path.getParent().getFileName().toString().equals("lebmods")) {
                            config.mappack = path.getParent().getFileName().toString();
                        }

                        if (config.mappack == null || config.mappack.isEmpty()) {
                            config.mappack = "Other";
                        }

                        config.mappack = FileHelper.cleanFileName(config.mappack);

                        config.filePath = path;

                        if (config.dimensionType == null) {
                            config.dimensionType = new Identifier("lem", config.id);

                            String dimJson = FileHelper.readFileFromZip(path, "world/dimension_type.json");
                            if (dimJson != null) {
                                DataResult<DimensionType> result = DimensionType.CODEC.parse(JsonOps.INSTANCE, ServerUtilsMod.getGson().fromJson(dimJson, JsonElement.class));
                                DimensionType type = result.result().get();

                                Registry<DimensionType> dimensionTypeRegistry = server.getRegistryManager().get(RegistryKeys.DIMENSION_TYPE);
                                ((RegistryUnfreezer) dimensionTypeRegistry).unfreeze();
                                Registry.register(dimensionTypeRegistry, config.dimensionType, type);

                                config.loadedDimensionType = type;
                            }
                        }

                        loadedMaps.put(config.id, config);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(System.nanoTime() - time);
    }


    public static MapSize autoSelectMapSize(LemModConfig config, int playerCount) {
        switch (HostOptions.selectedMapSize) {
            case SMALL -> {
                if (config.hassmall)
                    return MapSize.SMALL;
            }
            case LARGE -> {
                if (config.haslarge)
                    return MapSize.LARGE;
            }
            case LARGE_PLUS -> {
                if (config.haslargeplus)
                    return MapSize.LARGE_PLUS;
            }
            case REMASTERED -> {
                if (config.hasremastered)
                    return MapSize.REMASTERED;
            }
        }

        //no size available, or Auto selected
        if (playerCount >= 0 && playerCount <= 6) {
            if (config.hassmall) return MapSize.SMALL;
            if (config.haslarge) return MapSize.LARGE;
            if (config.haslargeplus) return MapSize.LARGE_PLUS;
            if (config.hasremastered) return MapSize.REMASTERED;
        }
        if (playerCount >= 7 && playerCount <= 8) {
            if (config.haslarge) return MapSize.LARGE;
            if (config.haslargeplus) return MapSize.LARGE_PLUS;
            if (config.hassmall) return MapSize.SMALL;
            if (config.hasremastered) return MapSize.REMASTERED;
        }
        if (playerCount >= 9) {
            if (config.haslargeplus) return MapSize.LARGE_PLUS;
            if (config.haslarge) return MapSize.LARGE;
            if (config.hassmall) return MapSize.SMALL;
            if (config.hasremastered) return MapSize.REMASTERED;
        }

        return null;
    }

    public static Path unZipMap(Path outputPath, LemModConfig config, MapSize mapSize) {
        Path lebmod = config.filePath;

        try (ZipFile zip = new ZipFile(lebmod)) {
            Enumeration<ZipArchiveEntry> entries = zip.getEntries();

            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                if (entry.getName().startsWith("world/" + mapSize.fileName + "/")) {
                    Path newOut = outputPath.resolve(entry.getName().replace("world/" + mapSize.fileName + "/", ""));
                    if (entry.isDirectory()) {
                        Files.createDirectories(newOut);
                    } else {
                        Files.createDirectories(newOut.getParent());
                        Files.copy(zip.getInputStream(entry), newOut);
                    }
                }
            }
            return outputPath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
