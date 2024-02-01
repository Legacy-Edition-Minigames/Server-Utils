package net.kyrptonaught.serverutils.customMapLoader;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.kyrptonaught.serverutils.FileHelper;
import net.kyrptonaught.serverutils.Module;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.customMapLoader.addons.BaseAddon;
import net.kyrptonaught.serverutils.customMapLoader.addons.BattleMapAddon;
import net.kyrptonaught.serverutils.customMapLoader.addons.LobbyAddon;
import net.kyrptonaught.serverutils.dimensionLoader.CustomDimHolder;
import net.kyrptonaught.serverutils.dimensionLoader.DimensionLoaderMod;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class CustomMapLoaderMod extends Module {

    public static final HashMap<Identifier, BattleMapAddon> BATTLE_MAPS = new HashMap<>();
    public static final HashMap<Identifier, LobbyAddon> LOBBY_MAPS = new HashMap<>();

    public static final HashMap<Identifier, BaseAddon> LOADED_MAPS = new HashMap<>();

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(this::discoverAddons);
    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        CustomMapLoaderCommands.registerCommands(dispatcher);
    }

    public static void prepareBattleMap(MinecraftServer server, Identifier addon, Identifier dimID) {
        BattleMapAddon config = CustomMapLoaderMod.BATTLE_MAPS.get(addon);
        MapSize mapSize = CustomMapLoaderMod.autoSelectMapSize(config, server.getCurrentPlayerCount());

        String[] coords = config.getMapDataForSize(mapSize).center_coords.split(" ");
        Vec3d pos = new Vec3d(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), Double.parseDouble(coords[2]));

        Path path = server.getSavePath(WorldSavePath.ROOT).resolve("dimensions").resolve(dimID.getNamespace()).resolve(dimID.getPath());
        CustomMapLoaderMod.unZipMap(path, config, mapSize);

        DimensionLoaderMod.loadDimension(dimID, config.dimensionType_id, server2 -> {
            CustomDimHolder holder = DimensionLoaderMod.loadedWorlds.get(dimID);

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ServerUtilsMod.SwitchableResourcepacksModule.execute(config.resource_pack, Collections.singleton(player));
                player.teleport(holder.world.asWorld(), pos.x, pos.y, pos.z, player.getYaw(), player.getPitch());
            }

        });
        LOADED_MAPS.put(dimID, config);
    }

    public static void prepareLobby(MinecraftServer server, Identifier addon, Identifier dimID) {
        LobbyAddon config = CustomMapLoaderMod.LOBBY_MAPS.get(addon);

        Path path = server.getSavePath(WorldSavePath.ROOT).resolve("dimensions").resolve(dimID.getNamespace()).resolve(dimID.getPath());
        CustomMapLoaderMod.unZipMap(path, config, null);

        DimensionLoaderMod.loadDimension(dimID, config.dimensionType_id, server2 -> teleportToLobby(dimID, server.getPlayerManager().getPlayerList(), null));
        LOADED_MAPS.put(dimID, config);
    }

    public static void teleportToLobby(Identifier dimID, Collection<ServerPlayerEntity> players, ServerPlayerEntity winner) {
        LobbyAddon config = (LobbyAddon) LOADED_MAPS.get(dimID);
        CustomDimHolder holder = DimensionLoaderMod.loadedWorlds.get(dimID);
        Random random = new Random();

        List<String> availCoords = new ArrayList<>(Arrays.asList(config.spawn_coords));

        if (winner != null) {
            ServerUtilsMod.SwitchableResourcepacksModule.execute(config.resource_pack, Collections.singleton(winner));

            if (config.winner_coords != null)
                tpPlayer(holder.world.asWorld(), winner, config.winner_coords.split(" "));
            else
                tpPlayer(holder.world.asWorld(), winner, availCoords.remove(random.nextInt(availCoords.size())).split(" "));
        }

        for (ServerPlayerEntity player : players) {
            ServerUtilsMod.SwitchableResourcepacksModule.execute(config.resource_pack, Collections.singleton(player));

            tpPlayer(holder.world.asWorld(), player, availCoords.remove(random.nextInt(availCoords.size())).split(" "));
        }
    }

    public static void tpPlayer(ServerWorld world, ServerPlayerEntity player, String[] coords) {
        float yaw = player.getYaw();
        float pitch = player.getPitch();

        if (coords.length == 5) {
            yaw = Float.parseFloat(coords[3]);
            pitch = Float.parseFloat(coords[4]);
        }

        Vec3d pos = new Vec3d(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), Double.parseDouble(coords[2]));
        player.teleport(world, pos.x, pos.y, pos.z, yaw, pitch);
    }

    public static void unloadMap(MinecraftServer server, Identifier dimID) {
        LOADED_MAPS.remove(dimID);
        DimensionLoaderMod.unLoadDimension(server, dimID, null);
    }

    public void discoverAddons(MinecraftServer server) {
        Path dir = FabricLoader.getInstance().getGameDir().resolve("lemaddons");
        try (Stream<Path> files = Files.walk(dir, 2)) {
            files.forEach(path -> {
                try {
                    if (!Files.isDirectory(path) && path.getFileName().toString().endsWith(".lemaddon")) {
                        String configJson = FileHelper.readFileFromZip(path, "config.json");

                        BaseAddon config = null;
                        JsonObject rawConfig = ServerUtilsMod.getGson().fromJson(configJson, JsonObject.class);

                        String type = rawConfig.get("addon_type").getAsString();
                        if (BattleMapAddon.TYPE.equals(type))
                            config = ServerUtilsMod.getGson().fromJson(rawConfig, BattleMapAddon.class);
                        if (LobbyAddon.TYPE.equals(type))
                            config = ServerUtilsMod.getGson().fromJson(rawConfig, LobbyAddon.class);

                        if (path.getParent().getFileName().toString().equals("base")) {
                            config.isBaseAddon = true;
                            config.addon_pack = "base_" + config.addon_pack;
                        } else if (!path.getParent().getFileName().toString().equals("lebmods")) {
                            config.addon_pack = path.getParent().getFileName().toString();
                        }

                        if (config.addon_pack == null || config.addon_pack.isEmpty()) {
                            config.addon_pack = "Other";
                        }

                        config.addon_pack = FileHelper.cleanFileName(config.addon_pack);

                        config.filePath = path;

                        if (config instanceof BattleMapAddon battleConfig) {
                            if (battleConfig.dimensionType_id == null) {
                                battleConfig.dimensionType_id = config.addon_id;
                                battleConfig.loadedDimensionType = addDimensionType(server, battleConfig.filePath, battleConfig.dimensionType_id);
                            }
                            BATTLE_MAPS.put(config.addon_id, battleConfig);
                        } else if (config instanceof LobbyAddon lobbyConfig) {
                            if (lobbyConfig.dimensionType_id == null) {
                                lobbyConfig.dimensionType_id = config.addon_id;
                                lobbyConfig.loadedDimensionType = addDimensionType(server, lobbyConfig.filePath, lobbyConfig.dimensionType_id);
                            }
                            LOBBY_MAPS.put(config.addon_id, lobbyConfig);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static MapSize autoSelectMapSize(BattleMapAddon config, int playerCount) {
        switch (HostOptions.selectedMapSize) {
            case SMALL -> {
                if (config.hasSize(MapSize.SMALL))
                    return MapSize.SMALL;
            }
            case LARGE -> {
                if (config.hasSize(MapSize.LARGE))
                    return MapSize.LARGE;
            }
            case LARGE_PLUS -> {
                if (config.hasSize(MapSize.LARGE_PLUS))
                    return MapSize.LARGE_PLUS;
            }
            case REMASTERED -> {
                if (config.hasSize(MapSize.REMASTERED))
                    return MapSize.REMASTERED;
            }
        }

        //no size available, or Auto selected
        if (playerCount >= 0 && playerCount <= 6) {
            if (config.hasSize(MapSize.SMALL)) return MapSize.SMALL;
            if (config.hasSize(MapSize.LARGE)) return MapSize.LARGE;
            if (config.hasSize(MapSize.LARGE_PLUS)) return MapSize.LARGE_PLUS;
            if (config.hasSize(MapSize.REMASTERED)) return MapSize.REMASTERED;
        }
        if (playerCount >= 7 && playerCount <= 8) {
            if (config.hasSize(MapSize.LARGE)) return MapSize.LARGE;
            if (config.hasSize(MapSize.LARGE_PLUS)) return MapSize.LARGE_PLUS;
            if (config.hasSize(MapSize.SMALL)) return MapSize.SMALL;
            if (config.hasSize(MapSize.REMASTERED)) return MapSize.REMASTERED;
        }
        if (playerCount >= 9) {
            if (config.hasSize(MapSize.LARGE_PLUS)) return MapSize.LARGE_PLUS;
            if (config.hasSize(MapSize.LARGE)) return MapSize.LARGE;
            if (config.hasSize(MapSize.SMALL)) return MapSize.SMALL;
            if (config.hasSize(MapSize.REMASTERED)) return MapSize.REMASTERED;
        }

        return null;
    }

    public static void unZipMap(Path outputPath, BaseAddon config, MapSize mapSize) {
        try (ZipFile zip = new ZipFile(config.filePath)) {
            Enumeration<ZipArchiveEntry> entries = zip.getEntries();

            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                if (entry.getName().startsWith(config.getDirectoryInZip(mapSize))) {
                    Path newOut = outputPath.resolve(entry.getName().replace(config.getDirectoryInZip(mapSize), ""));
                    if (entry.isDirectory()) {
                        Files.createDirectories(newOut);
                    } else {
                        Files.createDirectories(newOut.getParent());
                        Files.copy(zip.getInputStream(entry), newOut);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static DimensionType addDimensionType(MinecraftServer server, Path addonPath, Identifier dimID) {
        String dimJson = FileHelper.readFileFromZip(addonPath, "world/dimension_type.json");
        if (dimJson != null) {
            DataResult<DimensionType> result = DimensionType.CODEC.parse(JsonOps.INSTANCE, ServerUtilsMod.getGson().fromJson(dimJson, JsonElement.class));
            DimensionType type = result.result().get();

            Registry<DimensionType> dimensionTypeRegistry = server.getRegistryManager().get(RegistryKeys.DIMENSION_TYPE);
            ((RegistryUnfreezer) dimensionTypeRegistry).unfreeze();
            Registry.register(dimensionTypeRegistry, dimID, type);

            return type;
        }

        return null;
    }
}
