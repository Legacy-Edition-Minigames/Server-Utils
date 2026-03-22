package net.kyrptonaught.serverutils.backendLink.personatus;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.util.UndashedUuid;
import net.kyrptonaught.serverutils.CMDHelper;
import net.kyrptonaught.serverutils.ConfigManager;
import net.kyrptonaught.serverutils.Module;
import net.kyrptonaught.serverutils.backendLink.BackendServer;
import net.kyrptonaught.serverutils.backendLink.discordBridge.Integrations;
import net.kyrptonaught.serverutils.mixin.personatus.ServerLoginNetworkHandlerAccessor;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class PersonatusModule extends Module {

    public static boolean enabled = false;

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("personatus")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("enabled")
                        .then(CommandManager.argument("enable", BoolArgumentType.bool())
                                .executes(context -> {
                                    enabled = BoolArgumentType.getBool(context, "enable");
                                    Integrations.personatusEnable(context.getSource(), enabled);
                                    return 1;
                                })))
                .then(CommandManager.literal("checkSpoof")
                        .then(CommandManager.argument("player", StringArgumentType.word())
                                .executes(context -> {
                                    String player = StringArgumentType.getString(context, "player");
                                    try {
                                        String responseName = URLGetValue(false, "kvs/get/personatus/" + player, "value");
                                        if (responseName != null)
                                            context.getSource().sendFeedback(CMDHelper.getFeedbackLiteral(player + " is being spoofed as " + responseName), false);
                                        else
                                            context.getSource().sendFeedback(CMDHelper.getFeedbackLiteral(player + " is not spoofing"), false);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return 1;
                                })
                        ))
                .then(CommandManager.literal("seeDisguise")
                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                .executes(context -> {
                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                    context.getSource().sendFeedback(CMDHelper.getFeedbackLiteral(player.getNameForScoreboard() + " is actually " + ((PersonatusProfile) player.getGameProfile()).getRealProfile().getName()), false);
                                    return 1;
                                })))
                .then(CommandManager.literal("spoof")
                        .then(CommandManager.argument("player", StringArgumentType.word())
                                .then(CommandManager.argument("spoofedName", StringArgumentType.word())
                                        .executes(context -> {
                                            try {
                                                String player = StringArgumentType.getString(context, "player");
                                                String spoofedName = StringArgumentType.getString(context, "spoofedName");

                                                if (URLGet("kvs/set/personatus/" + player + "/" + spoofedName)) {
                                                    context.getSource().sendFeedback(CMDHelper.getFeedbackLiteral("Spoofing set. Please check the spoof first with /personatus checkSpoof " + player + " to verify. Relog to apply spoof."), false);
                                                    Integrations.personatusSpoof(context.getSource(), player, spoofedName);
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            return 1;
                                        }))))
                .then(CommandManager.literal("clearSpoof")
                        .then(CommandManager.argument("player", StringArgumentType.word())
                                .executes(context -> {
                                    String player = StringArgumentType.getString(context, "player");
                                    try {
                                        if (URLGet("kvs/reset/personatus/" + player)) {
                                            context.getSource().sendFeedback(CMDHelper.getFeedbackLiteral("Spoof reset"), false);
                                            Integrations.personatusClear(context.getSource(), player);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return 1;
                                }))));
    }

    public static String URLGetValue(boolean mojangURL, String url, String key) {
        String response = mojangURL ? BackendServer.getAlt(url) : BackendServer.get(url);

        if (response != null && !response.isEmpty()) {
            JsonObject obj = ConfigManager.getGSON().fromJson(response, JsonObject.class);
            if (obj != null && obj.has(key)) {
                if (obj.get(key) instanceof JsonNull) return null;
                return obj.get(key).getAsString();
            }
        }
        return null;
    }

    public static boolean URLGet(String url) {
        return BackendServer.get(url) != null;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static GameProfile checkProfile(ServerPlayNetworkHandler handler, GameProfile profile, JsonObject loginObj) {
        if (isEnabled()) {
            if (((ServerLoginNetworkHandlerAccessor) handler).getServer().getSessionService() instanceof YggdrasilMinecraftSessionService sessionService) {
                String responseName = URLGetValue(false, "kvs/get/personatus/" + profile.getName(), "value");
                if (responseName != null) {
                    String responseUUID = URLGetValue(true, "https://api.mojang.com/users/profiles/minecraft/" + responseName, "id");
                    if (responseUUID != null) {
                        UUID uuid = UndashedUuid.fromString(responseUUID);
                        GameProfile spoofed = sessionService.fetchProfile(uuid, true).profile();

                        ((PersonatusProfile) spoofed).setRealProfile(profile);
                        return spoofed;
                    }
                }
            }
        }

        return profile;
    }
}
