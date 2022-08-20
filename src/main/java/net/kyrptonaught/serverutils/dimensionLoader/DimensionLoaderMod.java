package net.kyrptonaught.serverutils.dimensionLoader;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.kyrptonaught.serverutils.FileHelper;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelStorage;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;
import xyz.nucleoid.fantasy.mixin.MinecraftServerAccess;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class DimensionLoaderMod {
    public static String MOD_ID = "dimensionloader";

    private static final HashMap<Identifier, RuntimeWorldHandle> loadedWorlds = new HashMap<>();

    public static void onInitialize() {
        CommandRegistrationCallback.EVENT.register(DimensionLoaderMod::registerCommand);
    }

    public static void loadDimension(MinecraftServer server, Identifier id, Identifier dimID) {
        Fantasy fantasy = Fantasy.get(server);

        if (loadedWorlds.containsKey(id))
            unLoadDimension(server, id);

        DimensionType dimensionType = server.getRegistryManager().get(Registry.DIMENSION_TYPE_KEY).get(dimID);
        if (dimensionType == null) {
            System.out.println("No Dim ID found");
            return;
        }

        if (!backupArenaMap(server, id, dimID)) {
            System.out.println("Failed creating temp directory");
            return;
        }

        RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
                .setDimensionType(dimensionType)
                .setGenerator(new VoidChunkGenerator(RegistryEntry.of(server.getRegistryManager().get(Registry.BIOME_KEY).get(new Identifier("minecraft:plains")))));

        loadedWorlds.put(id, fantasy.openTemporaryWorld(id, worldConfig));
    }

    public static void unLoadDimension(MinecraftServer server, Identifier id) {
        RuntimeWorldHandle world = loadedWorlds.remove(id);
        if (world != null)
            world.delete();
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

    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher, boolean b) {
        dispatcher.register(CommandManager.literal(MOD_ID)
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("prepareDimension")
                        .then(CommandManager.argument("id", IdentifierArgumentType.identifier())
                                .then(CommandManager.argument("dimnid", IdentifierArgumentType.identifier())
                                        .executes(context -> {
                                            Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
                                            Identifier dimId = IdentifierArgumentType.getIdentifier(context, "dimnid");
                                            loadDimension(context.getSource().getServer(), id, dimId);
                                            return 1;
                                        }))))
                .then(CommandManager.literal("unload")
                        .then(CommandManager.argument("id", IdentifierArgumentType.identifier())
                                .suggests(DimensionLoaderMod::getLoadedSuggestions)
                                .executes(context -> {
                                    Identifier id = IdentifierArgumentType.getIdentifier(context, "id");
                                    unLoadDimension(context.getSource().getServer(), id);
                                    return 1;
                                }))));
    }

    private static CompletableFuture<Suggestions> getLoadedSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        loadedWorlds.keySet().forEach(identifier -> builder.suggest(identifier.toString()));
        return builder.buildFuture();
    }
}
