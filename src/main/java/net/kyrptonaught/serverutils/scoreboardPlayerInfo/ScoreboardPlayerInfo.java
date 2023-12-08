package net.kyrptonaught.serverutils.scoreboardPlayerInfo;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.mixin.networking.accessor.ServerCommonNetworkHandlerAccessor;
import net.kyrptonaught.serverutils.Module;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ScoreboardPlayerInfo extends Module {
    private final static List<CustomObjective> objectives = new ArrayList<>();
    private static final CustomObjective protocolObjective = createObj("mcprotocolversion", "Client MC Protocol Version");
    private static final CustomObjective hasOptifineObjective = createObj("hasoptifine", "Client has Optifine");
    private static final CustomObjective hasLEMClientObjective = createObj("haslemclient", "Client has LEMClientHelper");
    private static final CustomObjective hasControllerModObjective = createObj("hascontroller", "Client has Controller Mod");
    private static final CustomObjective fabricClientObjective = createObj("fabricclient", "Client is using Fabric");
    private static final CustomObjective forgeClientObjective = createObj("forgeclient", "Client is using Forge");
    private static final CustomObjective bedrockClientObjective = createObj("bedrockclient", "Client is using Bedrock");
    private static final CustomObjective clientGUIScaleObjective = createObj("clientguiscale", "Client's GUI Scale");
    private static final CustomObjective clientPanScaleObjective = createObj("clientpanscale", "Client's Panorama Scale");

    private final static HashMap<ClientConnection, QueuedPlayerData> queuedPlayerData = new HashMap<>();

    public void onInitialize() {
        ScoreboardPlayerInfoNetworking.registerReceivePacket();

        ServerLifecycleEvents.SERVER_STARTED.register(ScoreboardPlayerInfo::registerScoreboardOBJs);
        ServerPlayConnectionEvents.INIT.register(ScoreboardPlayerInfo::onPlayerPreConnect);
        ServerPlayConnectionEvents.JOIN.register(ScoreboardPlayerInfo::onPlayerConnect);
    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal(getMOD_ID())
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("setAllScores")
                        .executes(context -> {
                            Scoreboard scoreboard = context.getSource().getServer().getScoreboard();
                            objectives.forEach(obj -> obj.addPlayerScoresToScoreboard(scoreboard));
                            return 1;
                        })
                        .then(CommandManager.argument("players", EntityArgumentType.players())
                                .executes(context -> {
                                    Scoreboard scoreboard = context.getSource().getServer().getScoreboard();
                                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
                                    objectives.forEach(obj -> obj.addPlayerScoresToScoreboard(scoreboard, players));
                                    return 1;
                                }))));
    }

    public static QueuedPlayerData getQueuedPlayerData(ClientConnection connection, boolean create) {
        if (create && !queuedPlayerData.containsKey(connection))
            queuedPlayerData.put(connection, new QueuedPlayerData());
        return queuedPlayerData.get(connection);
    }

    public static void registerScoreboardOBJs(MinecraftServer server) {
        ServerScoreboard scoreboard = server.getScoreboard();
        ScoreboardObjective[] objectives = scoreboard.getObjectives().toArray(ScoreboardObjective[]::new);
        for (int i = objectives.length - 1; i >= 0; i--) {
            if (objectives[i].getName().startsWith(ServerUtilsMod.MOD_ID + "."))
                scoreboard.removeObjective(objectives[i]);
        }

        ScoreboardPlayerInfo.objectives.forEach(obj -> obj.addToScoreboard(scoreboard));
    }

    public static void onPlayerPreConnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
        Scoreboard scoreboard = server.getScoreboard();
        ScoreboardPlayerInfo.objectives.forEach(obj -> obj.resetScore(scoreboard, handler.player));
    }

    public static void onPlayerConnect(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        if (queuedPlayerData.containsKey(((ServerCommonNetworkHandlerAccessor) handler).getConnection())) {
            QueuedPlayerData playerData = queuedPlayerData.remove(((ServerCommonNetworkHandlerAccessor) handler).getConnection());
            protocolObjective.setScore(handler.player, playerData.protocolVersion);
            setHasLEMClient(handler.player, playerData.hasLCH);
            setHasOptifine(handler.player, playerData.hasOptishit);
            setHasControllerMod(handler.player, playerData.hasController);
            setBedrockClient(handler.player, playerData.isBedrock);
            setGUIScale(handler.player, playerData.guiScale);
            setPanScale(handler.player, playerData.panScale);
        }

        if (ServerPlayNetworking.canSend(handler, new Identifier("fabric:registry/sync")))
            setFabricClient(handler.player, true);
    }

    public static void checkBrand(ServerPlayerEntity player, String brand) {
        if (brand.contains("forge"))
            setForgeClient(player, true);
        else if (brand.contains("fabric"))
            setFabricClient(player, true);
    }

    public static void setFabricClient(PlayerEntity player, boolean fabricClient) {
        fabricClientObjective.setScore(player, fabricClient ? 2 : 1);
    }

    public static void setForgeClient(PlayerEntity player, boolean forgeClient) {
        forgeClientObjective.setScore(player, forgeClient ? 2 : 1);
    }

    public static void setBedrockClient(PlayerEntity player, Boolean bedrockClient) {
        if (bedrockClient != null)
            bedrockClientObjective.setScore(player, bedrockClient ? 2 : 1);
    }

    public static void addClientConnectionProtocol(ClientConnection connection, int protocol) {
        getQueuedPlayerData(connection, true).protocolVersion = protocol;
    }

    public static void setHasLEMClient(PlayerEntity player, Boolean hasLEMClient) {
        if (hasLEMClient != null)
            hasLEMClientObjective.setScore(player, hasLEMClient ? 2 : 1);
    }

    public static void setHasOptifine(PlayerEntity player, Boolean hasOptifine) {
        if (hasOptifine != null)
            hasOptifineObjective.setScore(player, hasOptifine ? 2 : 1);
    }

    public static void setHasControllerMod(PlayerEntity player, Boolean hasController) {
        if (hasController != null)
            hasControllerModObjective.setScore(player, hasController ? 2 : 1);
    }

    public static void setGUIScale(PlayerEntity player, Integer scale) {
        if (scale != null)
            clientGUIScaleObjective.setScore(player, scale);
    }

    public static void setPanScale(PlayerEntity player, Integer scale) {
        if (scale != null)
            clientPanScaleObjective.setScore(player, scale);
    }

    public static CustomObjective createObj(String key, String displayName) {
        CustomObjective obj = new CustomObjective(key, displayName);
        objectives.add(obj);
        return obj;
    }
}
