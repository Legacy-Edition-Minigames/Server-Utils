package net.kyrptonaught.serverutils.personatus;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.kyrptonaught.serverutils.ModuleWConfig;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.backendServer.BackendServerModule;
import net.kyrptonaught.serverutils.discordBridge.Integrations;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;


public class PersonatusModule extends ModuleWConfig<PersonatusConfig> {

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("personatus")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("enabled")
                        .then(CommandManager.argument("enable", BoolArgumentType.bool())
                                .executes(context -> {
                                    getConfig().enabled = BoolArgumentType.getBool(context, "enable");
                                    saveConfig();
                                    Integrations.personatusEnable(context.getSource().getName(), getConfig().enabled);
                                    return 1;
                                })))
                .then(CommandManager.literal("checkSpoof")
                        .then(CommandManager.argument("player", StringArgumentType.word())
                                .executes(context -> {
                                    String player = StringArgumentType.getString(context, "player");
                                    try {
                                        String responseName = URLGetValue(false, "kvs/get/personatus/" + player, "value");
                                        if (responseName != null)
                                            context.getSource().sendFeedback(Text.literal(player + " is being spoofed as " + responseName), false);
                                        else
                                            context.getSource().sendFeedback(Text.literal(player + " is not spoofing"), false);
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
                                    context.getSource().sendFeedback(Text.literal(player.getEntityName() + " is actually " + ((PersonatusProfile) player.getGameProfile()).getRealProfile().getName()), false);
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
                                                    context.getSource().sendFeedback(Text.literal("Spoofing set. Please check the spoof first with /personatus checkSpoof " + player + " to verify. Relog to apply spoof."), false);
                                                    Integrations.personatusSpoof(context.getSource().getName(), player, spoofedName);
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
                                            context.getSource().sendFeedback(Text.literal("Spoof reset"), false);
                                            Integrations.personatusClear(context.getSource().getName(), player);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return 1;
                                }))));
    }

    public static String URLGetValue(boolean mojangURL, String url, String key) {
        String response = mojangURL ? BackendServerModule.getAlt(url) : BackendServerModule.get(url);

        if (response != null && !response.isEmpty()) {
            JsonObject obj = ServerUtilsMod.config.getGSON().fromJson(response, JsonObject.class);
            if (obj != null && obj.has(key)) {
                if (obj.get(key) instanceof JsonNull) return null;
                return obj.get(key).getAsString();
            }
        }
        return null;
    }

    public static boolean URLGet(String url) {
        return BackendServerModule.get(url) != null;
    }

    public static boolean isEnabled() {
        return ServerUtilsMod.personatusModule.getConfig().enabled;
    }

    @Override
    public PersonatusConfig createDefaultConfig() {
        return new PersonatusConfig();
    }
}
