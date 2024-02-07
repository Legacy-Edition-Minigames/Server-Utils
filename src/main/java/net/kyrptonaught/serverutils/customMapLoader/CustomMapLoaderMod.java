package net.kyrptonaught.serverutils.customMapLoader;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.kyrptonaught.serverutils.Module;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.chestTracker.ChestTrackerMod;
import net.kyrptonaught.serverutils.customMapLoader.addons.BattleMapAddon;
import net.kyrptonaught.serverutils.customMapLoader.addons.LobbyAddon;
import net.kyrptonaught.serverutils.customWorldBorder.CustomWorldBorderMod;
import net.kyrptonaught.serverutils.dimensionLoader.CustomDimHolder;
import net.kyrptonaught.serverutils.dimensionLoader.DimensionLoaderMod;
import net.kyrptonaught.serverutils.playerlockdown.PlayerLockdownMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.nio.file.Path;
import java.util.*;

public class CustomMapLoaderMod extends Module {

    public static final HashMap<Identifier, BattleMapAddon> BATTLE_MAPS = new HashMap<>();
    public static final HashMap<Identifier, LobbyAddon> LOBBY_MAPS = new HashMap<>();

    private static final HashMap<Identifier, LoadedBattleMapInstance> LOADED_BATTLE_MAPS = new HashMap<>();
    private static final HashMap<Identifier, LobbyAddon> LOADED_LOBBIES = new HashMap<>();

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(IO::discoverAddons);
    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        CustomMapLoaderCommands.registerCommands(dispatcher);
    }

    public static void battleLoad(MinecraftServer server, Identifier addon, Identifier dimID, boolean centralSpawnEnabled, Collection<ServerPlayerEntity> players, Collection<CommandFunction<ServerCommandSource>> functions) {
        BattleMapAddon config = CustomMapLoaderMod.BATTLE_MAPS.get(addon);
        MapSize mapSize = CustomMapLoaderMod.autoSelectMapSize(config, server.getCurrentPlayerCount());

        Path path = server.getSavePath(WorldSavePath.ROOT).resolve("dimensions").resolve(dimID.getNamespace()).resolve(dimID.getPath());
        IO.unZipMap(path, config, mapSize);

        DimensionLoaderMod.loadDimension(dimID, config.dimensionType_id, server2 -> {
            LoadedBattleMapInstance instance = new LoadedBattleMapInstance(centralSpawnEnabled, mapSize, config, dimID);
            battlePrepare(instance, players);
            battleSpawn(instance, players);

            if (functions != null) {
                for (CommandFunction<ServerCommandSource> commandFunction : functions) {
                    server.getCommandFunctionManager().execute(commandFunction, server.getCommandSource().withLevel(2).withSilent());
                }
            }
            LOADED_BATTLE_MAPS.put(dimID, new LoadedBattleMapInstance(centralSpawnEnabled, mapSize, config, dimID));
        });
    }

    private static void battlePrepare(LoadedBattleMapInstance instance, Collection<ServerPlayerEntity> players) {
        ServerWorld world = instance.getWorld();
        BattleMapAddon.MapSizeConfig sizedConfig = instance.getSizedAddon();
        ParsedPlayerCoords centerPos = parseVec3D(sizedConfig.center_coords);

        CustomWorldBorderMod.getCustomWorldBorderManager(world).setCustomWorldBorder(world, parseBlockPos(sizedConfig.world_border_coords_1), parseBlockPos(sizedConfig.world_border_coords_2));

        ChestTrackerMod.reset(world.getServer());
        for (String pos : sizedConfig.chest_tracker_coords) {
            ChestTrackerMod.trackedChests.add(parseBlockPos(pos));
        }

        //todo datapack interactables

        for (ServerPlayerEntity player : players) {
            Collection<ServerPlayerEntity> single = Collections.singleton(player);
            ParsedPlayerCoords playerPos = centerPos.fillPlayer(player);

            ServerUtilsMod.SwitchableResourcepacksModule.execute(instance.getAddon().resource_pack, single);

            player.teleport(world, playerPos.x(), playerPos.y(), playerPos.z(), 0, 0);

            PlayerLockdownMod.executeLockdown(single, true);
            PlayerLockdownMod.executeFreeze(single, playerPos.pos, true);
        }
    }

    private static void battleSpawn(LoadedBattleMapInstance instance, Collection<ServerPlayerEntity> players) {
        List<String> centerSpawns = new ArrayList<>(Arrays.asList(instance.getSizedAddon().center_spawn_coords));
        List<String> randomSpawns = new ArrayList<>(Arrays.asList(instance.getSizedAddon().random_spawn_coords));


        ParsedPlayerCoords centerPos = parseVec3D(instance.getSizedAddon().center_coords);

        for (ServerPlayerEntity player : players) {
            String raw;
            if (!centerSpawns.isEmpty() && instance.isCentralSpawnEnabled()) {
                raw = centerSpawns.remove(instance.getWorld().random.nextInt(centerSpawns.size()));
            } else {
                if (randomSpawns.isEmpty())
                    randomSpawns = new ArrayList<>(Arrays.asList(instance.getSizedAddon().random_spawn_coords));

                raw = randomSpawns.remove(instance.getWorld().random.nextInt(randomSpawns.size()));
            }
            ParsedPlayerCoords playerPos = parseVec3D(raw);
            float yaw = MathHelper.wrapDegrees((float) (MathHelper.atan2(centerPos.z() - playerPos.z(), centerPos.x() - playerPos.x()) * 57.2957763671875) - 90.0f);
            player.teleport(instance.getWorld(), playerPos.x(), playerPos.y(), playerPos.z(), yaw, 0);
            PlayerLockdownMod.executeFreeze(Collections.singleton(player), playerPos.pos, true);
        }
    }

    public static void battleTp(Identifier dimID, Collection<ServerPlayerEntity> players) {
        LoadedBattleMapInstance instance = LOADED_BATTLE_MAPS.get(dimID);

        ParsedPlayerCoords centerPos = parseVec3D(instance.getSizedAddon().center_coords);

        List<String> randomSpawns = new ArrayList<>(Arrays.asList(instance.getSizedAddon().random_spawn_coords));
        for (ServerPlayerEntity player : players) {
            ServerUtilsMod.SwitchableResourcepacksModule.execute(instance.getAddon().resource_pack, Collections.singleton(player));

            if (randomSpawns.isEmpty())
                randomSpawns = new ArrayList<>(Arrays.asList(instance.getSizedAddon().random_spawn_coords));

            String raw = randomSpawns.remove(instance.getWorld().random.nextInt(randomSpawns.size()));

            ParsedPlayerCoords playerPos = parseVec3D(raw);
            float yaw = MathHelper.wrapDegrees((float) (MathHelper.atan2(centerPos.z() - playerPos.z(), centerPos.x() - playerPos.x()) * 57.2957763671875) - 90.0f);
            player.teleport(instance.getWorld(), playerPos.x(), playerPos.y(), playerPos.z(), yaw, 0);
        }
    }

    public static void prepareLobby(MinecraftServer server, Identifier addon, Identifier dimID, Collection<ServerPlayerEntity> players, Collection<CommandFunction<ServerCommandSource>> functions) {
        LobbyAddon config = CustomMapLoaderMod.LOBBY_MAPS.get(addon);

        Path path = server.getSavePath(WorldSavePath.ROOT).resolve("dimensions").resolve(dimID.getNamespace()).resolve(dimID.getPath());
        IO.unZipMap(path, config, null);

        DimensionLoaderMod.loadDimension(dimID, config.dimensionType_id, server2 -> {
            teleportToLobby(dimID, players, null);

            if (functions != null) {
                for (CommandFunction<ServerCommandSource> commandFunction : functions) {
                    server.getCommandFunctionManager().execute(commandFunction, server.getCommandSource().withLevel(2).withSilent());
                }
            }
        });

        LOADED_LOBBIES.put(dimID, config);
    }

    public static void teleportToLobby(Identifier dimID, Collection<ServerPlayerEntity> players, ServerPlayerEntity winner) {
        LobbyAddon config = LOADED_LOBBIES.get(dimID);
        CustomDimHolder holder = DimensionLoaderMod.loadedWorlds.get(dimID);
        Random random = new Random();

        List<String> availCoords = new ArrayList<>(Arrays.asList(config.spawn_coords));

        if (winner != null) {
            if (config.winner_coords != null) {
                tpPlayer(holder.world.asWorld(), winner, config.winner_coords.split(" "), config.resource_pack);
            } else {
                players.add(winner);
            }
        }

        for (ServerPlayerEntity player : players) {
            if (availCoords.isEmpty())
                availCoords = new ArrayList<>(Arrays.asList(config.spawn_coords));

            tpPlayer(holder.world.asWorld(), player, availCoords.remove(random.nextInt(availCoords.size())).split(" "), config.resource_pack);
        }
    }

    private static void tpPlayer(ServerWorld world, ServerPlayerEntity player, String[] coords, String resourcePack) {
        ServerUtilsMod.SwitchableResourcepacksModule.execute(resourcePack, Collections.singleton(player));

        float yaw = player.getYaw();
        float pitch = player.getPitch();

        if (coords.length == 5) {
            yaw = Float.parseFloat(coords[3]);
            pitch = Float.parseFloat(coords[4]);
        }

        Vec3d pos = new Vec3d(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), Double.parseDouble(coords[2]));
        player.teleport(world, pos.x, pos.y, pos.z, yaw, pitch);
    }

    public static void unloadMap(MinecraftServer server, Identifier dimID, Collection<CommandFunction<ServerCommandSource>> functions) {
        LOADED_BATTLE_MAPS.remove(dimID);
        LOADED_LOBBIES.remove(dimID);
        DimensionLoaderMod.unLoadDimension(server, dimID, functions);
    }

    private static BlockPos parseBlockPos(String coords) {
        String[] coords_split = coords.split(" ");
        return new BlockPos(Integer.parseInt(coords_split[0]), Integer.parseInt(coords_split[1]), Integer.parseInt(coords_split[2]));
    }

    private static ParsedPlayerCoords parseVec3D(String coords) {
        String[] coords_split = coords.split(" ");
        ParsedPlayerCoords playerCoords = new ParsedPlayerCoords(new Vec3d(Double.parseDouble(coords_split[0]), Double.parseDouble(coords_split[1]), Double.parseDouble(coords_split[2])));
        if (coords_split.length == 5) {
            playerCoords.fillYawPitch(Float.parseFloat(coords_split[3]), Float.parseFloat(coords_split[4]));
        }
        return playerCoords;
    }

    private static MapSize autoSelectMapSize(BattleMapAddon config, int playerCount) {
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

    public static final class ParsedPlayerCoords {
        private final Vec3d pos;
        private Float yaw;
        private Float pitch;

        public ParsedPlayerCoords(Vec3d pos, float yaw, float pitch) {
            this.pos = pos;
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public ParsedPlayerCoords(Vec3d pos) {
            this.pos = pos;
            this.yaw = null;
            this.pitch = null;
        }

        public ParsedPlayerCoords fillPlayer(ServerPlayerEntity player) {
            return new ParsedPlayerCoords(pos, yaw == null ? player.getYaw() : yaw, pitch == null ? player.getPitch() : pitch);
        }

        public void fillYawPitch(float yaw, float pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public Vec3d pos() {
            return pos;
        }

        public double x() {
            return pos.x;
        }

        public double y() {
            return pos.y;
        }

        public double z() {
            return pos.z;
        }

        public float yaw() {
            return yaw;
        }

        public float pitch() {
            return pitch;
        }
    }
}
