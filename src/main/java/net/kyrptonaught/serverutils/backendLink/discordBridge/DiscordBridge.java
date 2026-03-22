package net.kyrptonaught.serverutils.backendLink.discordBridge;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.backendLink.BackendServer;
import net.kyrptonaught.serverutils.backendLink.personatus.PersonatusModule;
import net.kyrptonaught.serverutils.backendLink.prohibitor.ProhibitorModule;
import net.kyrptonaught.serverutils.backendLink.prohibitor.actions.ChatDisablerAction;
import net.kyrptonaught.serverutils.backendLink.prohibitor.actions.LinkingDisablerAction;
import net.kyrptonaught.serverutils.userConfig.UserConfigStorage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.UUID;

public class DiscordBridge {
    private static WebSocketClient socket;
    private static MinecraftServer server;

    public static void earlyInit() {
        socket = new SimpleSocket(getURI()) {

            @Override
            public void onMessage(String message) {
                onReceivedMessage(message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                try {
                    Thread.sleep(5000);
                    if (server != null) server.execute(this::reconnect);
                    else reconnect();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        try {
            socket.connectBlocking();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            DiscordBridge.server = server;
            sendIdentifier();

            Logger rootLogger = (Logger) LogManager.getRootLogger();
            BridgeLogger appender = new BridgeLogger("Bridge Logger", null, null);
            appender.start();
            rootLogger.addAppender(appender);

            server.sendMessage(Text.literal("Server Started"));
            sendMessage(Text.literal("Server Started"), 0xffffff);
        });

        ServerLifecycleEvents.SERVER_STOPPED.register(server2 -> {
            sendMessage(Text.literal("Server Stopped"), 0xffffff);
            server.sendMessage(Text.literal("Server Stopped"));
            socket.close();
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server2) -> {
            Integrations.sendLeaveMessage(Text.translatable("multiplayer.player.left", handler.player.getDisplayName()).formatted(Formatting.YELLOW));
        });
    }

    private static URI getURI() {
        try {
            return new URI(BackendServer.getApiUrl("bridge/" + BackendServer.config.bridgeName).replace("http://", "ws://"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void onReceivedMessage(String message) {
        JsonObject obj = ServerUtilsMod.getGson().fromJson(message, JsonObject.class);

        if (obj.get("type").getAsString().equals("chat_approved")) {
            Text player = TextCodecs.CODEC.parse(JsonOps.INSTANCE, obj.get("name_text")).result().get();
            Text msg = TextCodecs.CODEC.parse(JsonOps.INSTANCE, obj.get("msg")).result().get();
            server.getPlayerManager().broadcast(Text.literal("<").append(player).append("> ").append(msg), false);
        } else if (obj.get("type").getAsString().equals("chat")) {
            Text msg = TextCodecs.CODEC.parse(JsonOps.INSTANCE, obj.get("msg")).result().get();
            server.getPlayerManager().broadcast(msg, false);
        } else if (obj.get("type").getAsString().equals("info_request")) {
            sendInfoMessage();
        } else if (obj.get("type").getAsString().equals("personatus_request")) {
            JsonObject obj2 = new JsonObject();
            obj2.addProperty("type", "personatus_reply");
            obj2.addProperty("status", PersonatusModule.enabled);
            socket.send(obj2.toString());
        } else if (obj.get("type").getAsString().equals("link_success")) {
            String uuid = obj.getAsJsonObject("link").get("mcUUID").getAsString();
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(UUID.fromString(uuid));
            if (player != null) {
                UserConfigStorage.overwritePlayer(player, obj.getAsJsonObject("integrations"));
                player.closeHandledScreen();
                player.sendMessageToClient(Text.literal("Linking successful"), false);
            }
        } else if (obj.get("type").getAsString().equals("prohibitor")) {
            ProhibitorModule.serverMessage(server, obj);
        } else if (obj.get("type").getAsString().equals("chat_disabler")) {
            ChatDisablerAction.chatDisable(obj.get("who").getAsString(), obj.get("enabled").getAsBoolean(), text -> server.getPlayerManager().broadcast(text, false));
        } else if (obj.get("type").getAsString().equals("linking_disabler")) {
            LinkingDisablerAction.linkDisabler(obj.get("who").getAsString(), obj.get("enabled").getAsBoolean(), text -> {
            });
        }
    }

    private static void encodeText(JsonObject obj, String name, Text text) {
        obj.add(name, TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, text).get().orThrow());
    }

    private static boolean canSendMessage() {
        return socket != null && socket.isOpen() && !socket.isClosed();
    }

    public static void sendMessage(JsonObject obj) {
        if (!canSendMessage()) return;
        socket.send(obj.toString());
    }

    public static void sendMessage(Text message) {
        sendMessage(message, 0xffffff);
    }

    public static void sendMessage(Text message, int color) {
        if (!canSendMessage()) return;

        JsonObject obj = new JsonObject();
        obj.addProperty("type", "game");
        obj.addProperty("color", color);
        encodeText(obj, "msg", message);
        socket.send(obj.toString());
    }

    public static void sendChatMessage(ServerPlayerEntity player, String message, boolean isMuted) {
        if (!canSendMessage()) return;

        JsonObject obj = new JsonObject();
        obj.addProperty("type", "chat");
        obj.addProperty("display_name", player.getNameForScoreboard());
        encodeText(obj, "name_text", player.getDisplayName());
        obj.addProperty("player_uuid", player.getUuidAsString());
        obj.addProperty("msg", message);
        obj.addProperty("muted", isMuted);
        socket.send(obj.toString());
    }

    public static void sendLogMessage(String message) {
        if (!canSendMessage()) return;

        JsonObject obj = new JsonObject();
        obj.addProperty("type", "log");
        obj.addProperty("server_name", BackendServer.config.serverName);
        obj.addProperty("msg", message);
        socket.send(obj.toString());
    }

    public static void sendLogMessage(Text message, boolean ping) {
        if (!canSendMessage()) return;

        JsonObject obj = new JsonObject();
        obj.addProperty("type", "log_text");
        obj.addProperty("server_name", BackendServer.config.serverName);
        obj.addProperty("ping", ping);
        encodeText(obj, "msg", message);
        socket.send(obj.toString());
    }

    public static void sendLockChannel(boolean locked) {
        if (!canSendMessage()) return;

        JsonObject obj = new JsonObject();
        obj.addProperty("type", "lock");
        obj.addProperty("locked", locked);
        socket.send(obj.toString());
    }

    public static void sendIdentifier() {
        if (!canSendMessage()) return;
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "identifier");
        obj.addProperty("name", BackendServer.config.serverName);
        socket.send(obj.toString());
    }

    public static void sendInfoMessage() {
        if (!canSendMessage()) return;

        JsonObject obj = new JsonObject();
        obj.addProperty("type", "info_reply");

        JsonArray playerArray = new JsonArray();
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            JsonObject playerOBJ = new JsonObject();
            playerOBJ.addProperty("name", player.getNameForScoreboard());
            playerOBJ.addProperty("isSpectator", false);
            playerOBJ.addProperty("latency", player.networkHandler.getLatency());
            playerArray.add(playerOBJ);
        }
        obj.add("players", playerArray);

        JsonArray ttArray = new JsonArray(server.getTickTimes().length);
        for (long tt : server.getTickTimes()) {
            ttArray.add(tt);
        }

        obj.addProperty("player_count", server.getMaxPlayerCount());
        obj.addProperty("spectator_count", 0);

        obj.add("server_tick_times", ttArray);
        obj.addProperty("total_memory", Runtime.getRuntime().totalMemory());
        obj.addProperty("free_memory", Runtime.getRuntime().freeMemory());
        socket.send(obj.toString());
    }

    public static void sendGameStartInfo(Text map, String size, String players, String spectators, Collection<Pair<Text, Text>> changedRules) {
        if (!canSendMessage()) return;

        JsonObject obj = new JsonObject();
        obj.addProperty("type", "game_start_info");
        encodeText(obj, "map_name", map);
        obj.addProperty("map_size", size);
        obj.addProperty("player_count", players);
        obj.addProperty("spectator_count", spectators);

        JsonArray rules = new JsonArray();
        for (Pair<Text, Text> rule : changedRules) {
            JsonObject entry = new JsonObject();
            encodeText(entry, "key", rule.getLeft());
            encodeText(entry, "value", rule.getRight());
            rules.add(entry);
        }
        obj.add("changed_rules", rules);
        socket.send(obj.toString());
    }
}

