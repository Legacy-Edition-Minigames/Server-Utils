package net.kyrptonaught.serverutils.backendLink.prohibitor;

import com.google.common.net.InetAddresses;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.serialization.JsonOps;
import com.mojang.util.UndashedUuid;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyrptonaught.serverutils.ModuleWConfig;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.backendLink.BackendServer;
import net.kyrptonaught.serverutils.backendLink.discordBridge.DiscordBridge;
import net.kyrptonaught.serverutils.userConfig.UserConfigStorage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

import java.net.InetSocketAddress;
import java.util.UUID;

public class ProhibitorModule extends ModuleWConfig<ProhibitorConfig> {

    @Override
    public void onInitialize() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, player, params) -> {
            if (!config().globalChatEnabled) {
                player.sendMessage(Text.translatable("chatdisabler.chatdisabled"), false);
                return false;
            }
            DiscordBridge.sendChatMessage(player, message.getSignedContent(), !canChat(player));
            return false;
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            BackendServer.earlyLogin(handler, handler.player.getGameProfile());
            server.execute(() -> {
                sendJoinMessages(handler.player);
                UserConfigStorage.onLoad(handler.player);
            });
        });
    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        ProhibitorCommands.registerCommands(dispatcher);
    }

    public static JsonObject canJoin(ServerPlayNetworkHandler handler, GameProfile profile) {
        JsonObject obj = new JsonObject();
        obj.add("profile", Codecs.GAME_PROFILE_WITH_PROPERTIES.encodeStart(JsonOps.INSTANCE, profile).get().orThrow());
        obj.addProperty("ip", getIp(handler));
        return ServerUtilsMod.getGson().fromJson(BackendServer.get("prohibitor/joincheck/NONE", obj.toString()), JsonObject.class).getAsJsonObject();
    }

    public static boolean canChat(ServerPlayerEntity player) {
        return !UserConfigStorage.getValue(player, new Identifier("ismuted")).equals("true");
    }

    public static GameProfile checkProfile(ServerPlayNetworkHandler handler, GameProfile profile, JsonObject loginObj) {
        if (loginObj.get("isSkinBanned").getAsBoolean()) {
            return new GameProfile(profile.getId(), profile.getName());
        }

        return profile;
    }

    public static void sendJoinMessages(ServerPlayerEntity player) {
        if (UserConfigStorage.getValue(player, new Identifier("ismuted")).equals("true")) {
            JsonObject obj = ServerUtilsMod.getGson().fromJson(UserConfigStorage.getValue(player, new Identifier("mutemessage")), JsonObject.class);
            player.sendMessage(TextCodecs.CODEC.parse(JsonOps.INSTANCE, obj).result().get(), false);
        }
        if (UserConfigStorage.getValue(player, new Identifier("isskinbanned")).equals("true")) {
            JsonObject obj = ServerUtilsMod.getGson().fromJson(UserConfigStorage.getValue(player, new Identifier("skinmessage")), JsonObject.class);
            player.sendMessage(TextCodecs.CODEC.parse(JsonOps.INSTANCE, obj).result().get(), false);
        }
    }

    private static String getIp(ServerPlayNetworkHandler handler) {
        return handler.getConnectionAddress() instanceof InetSocketAddress inetSocketAddress
                ? InetAddresses.toAddrString(inetSocketAddress.getAddress())
                : "<unknown>";
    }

    public static void serverMessage(MinecraftServer server, JsonObject obj) {
        String action = obj.get("action").getAsString();
        String uuid = obj.get("uuid").getAsString();
        UUID uuidParsed = uuid.contains("-") ? UUID.fromString(uuid) : UndashedUuid.fromString(uuid);
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuidParsed);
        if (player != null) {
            if (action.equals("still_muted")) {
                player.sendMessage(Text.translatable("prohibitor.mute.cannotsent"), false);
                return;
            }

            Text reason = TextCodecs.CODEC.parse(JsonOps.INSTANCE, obj.get("reason")).result().get();
            switch (action) {
                case "kick", "ban" -> {
                    player.networkHandler.disconnect(reason);
                }
                case "mute" -> {
                    player.sendMessage(reason);
                    UserConfigStorage.setValue(player, new Identifier("ismuted"), "true");
                }
                case "unmute" -> {
                    player.sendMessage(Text.translatable("prohibitor.mute.unmuted"));
                    player.sendMessage(reason);
                    UserConfigStorage.setValue(player, new Identifier("ismuted"), "false");
                }
            }
        }
    }

    @Override
    public ProhibitorConfig createDefaultConfig() {
        return new ProhibitorConfig();
    }

    public static ProhibitorConfig config() {
        return ServerUtilsMod.ProhibitorModule.getConfig();
    }
}