package net.kyrptonaught.serverutils.whitelistSync;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.kyrptonaught.serverutils.ModuleWConfig;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.backendServer.BackendServerModule;
import net.kyrptonaught.serverutils.discordBridge.Integrations;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Whitelist;
import net.minecraft.server.WhitelistEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.JsonHelper;

import java.util.Collection;

public class WhitelistSyncMod extends ModuleWConfig<WhitelistSyncConfig> {

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (getConfig().syncOnStart) {
                boolean success = syncWhitelist(server);
                if (success) System.out.println("Synced Whitelist");
                else System.out.println("Error syncing whitelist");
            }
        });
    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("syncedwhitelist")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("add")
                        .then(CommandManager.argument("targets", GameProfileArgumentType.gameProfile())
                                .executes(context -> {
                                    addWhitelist(context.getSource(), GameProfileArgumentType.getProfileArgument(context, "targets"));
                                    return 1;
                                })))
                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("targets", GameProfileArgumentType.gameProfile())
                                .executes(context -> {
                                    removeWhitelist(context.getSource(), GameProfileArgumentType.getProfileArgument(context, "targets"));
                                    return 1;
                                })))
                .then(CommandManager.literal("clear").executes(context -> {
                    Whitelist whitelist = context.getSource().getServer().getPlayerManager().getWhitelist();

                    for (WhitelistEntry entry : whitelist.values().toArray(WhitelistEntry[]::new)) {
                        whitelist.remove(entry);
                    }

                    BackendServerModule.asyncPost("whitelist/clear", (success, stringHttpResponse) -> {
                        if (success) output(context.getSource(), Text.literal("Cleared whitelist"));
                        else output(context.getSource(), Text.literal("Error syncing whitelist clear"));

                        if (getConfig().attemptKickOnUpdate)
                            context.getSource().getServer().kickNonWhitelistedPlayers(context.getSource());
                    });
                    return 1;
                }))
                .then(CommandManager.literal("sync").executes(context -> {
                    boolean success = syncWhitelist(context.getSource().getServer());
                    if (success) {
                        output(context.getSource(), Text.translatable("commands.whitelist.reloaded"));
                        CharSequence[] strings = context.getSource().getServer().getPlayerManager().getWhitelistedNames();
                        if (strings.length == 0)
                            output(context.getSource(), Text.translatable("commands.whitelist.none"));
                        else
                            output(context.getSource(), Text.translatable("commands.whitelist.list", strings.length, String.join(", ", strings)));

                        if (getConfig().attemptKickOnUpdate)
                            context.getSource().getServer().kickNonWhitelistedPlayers(context.getSource());
                    } else output(context.getSource(), Text.literal("Error syncing whitelist sync"));
                    return 1;
                }))
                .then(CommandManager.literal("syncOnStart").then(CommandManager.argument("enabled", BoolArgumentType.bool()).executes(context -> {
                    getConfig().syncOnStart = BoolArgumentType.getBool(context, "enabled");
                    saveConfig();
                    output(context.getSource(), Text.literal("Set SyncOnStart to " + getConfig().syncOnStart));
                    return 1;
                })))
        );
    }

    public static void addWhitelist(ServerCommandSource source, Collection<GameProfile> targets) {
        Whitelist whitelist = source.getServer().getPlayerManager().getWhitelist();

        for (GameProfile gameProfile : targets) {
            WhitelistEntry whitelistEntry = new WhitelistEntry(gameProfile);
            whitelist.add(whitelistEntry);
            BackendServerModule.asyncPost("whitelist/add/" + gameProfile.getId().toString() + "/" + gameProfile.getName(), (success, stringHttpResponse) -> {
                if (success)
                    output(source, Text.translatable("commands.whitelist.add.success", Texts.toText(gameProfile)));
                else
                    output(source, Text.literal("Error syncing whitelist add: ").append(Texts.toText(gameProfile)));
            });
        }
    }

    public static void removeWhitelist(ServerCommandSource source, Collection<GameProfile> targets) {
        Whitelist whitelist = source.getServer().getPlayerManager().getWhitelist();

        for (GameProfile gameProfile : targets) {
            WhitelistEntry whitelistEntry = new WhitelistEntry(gameProfile);
            whitelist.remove(whitelistEntry);
            BackendServerModule.asyncPost("whitelist/remove/" + gameProfile.getId().toString() + "/" + gameProfile.getName(), (success, stringHttpResponse) -> {
                if (success)
                    output(source, Text.translatable("commands.whitelist.remove.success", Texts.toText(gameProfile)));
                else
                    output(source, Text.literal("Error syncing whitelist remove: ").append(Texts.toText(gameProfile)));

                if (ServerUtilsMod.whitelistSyncMod.getConfig().attemptKickOnUpdate)
                    source.getServer().kickNonWhitelistedPlayers(source);
            });
        }
    }

    private static boolean syncWhitelist(MinecraftServer server) {
        Whitelist whitelist = server.getPlayerManager().getWhitelist();

        String result = BackendServerModule.get("whitelist/get");
        if (result != null) {
            for (WhitelistEntry entry : whitelist.values().toArray(WhitelistEntry[]::new)) {
                whitelist.remove(entry);
            }

            JsonArray jsonArray = ServerUtilsMod.getGson().fromJson(result, JsonArray.class);

            for (JsonElement jsonElement : jsonArray) {
                JsonObject jsonObject = JsonHelper.asObject(jsonElement, "entry");
                WhitelistEntry serverConfigEntry = new WhitelistEntry(jsonObject);

                whitelist.add(serverConfigEntry);
            }
            return true;
        }
        return false;
    }

    private static void output(ServerCommandSource source, Text text) {
        source.sendFeedback(text, false);
        Integrations.whitelistSync(source, text);
    }

    @Override
    public WhitelistSyncConfig createDefaultConfig() {
        return new WhitelistSyncConfig();
    }
}
