package net.kyrptonaught.serverutils.backendLink.prohibitor;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.kyrptonaught.serverutils.backendLink.BackendServer;
import net.kyrptonaught.serverutils.backendLink.Broadcaster;
import net.kyrptonaught.serverutils.backendLink.discordBridge.Integrations;
import net.kyrptonaught.serverutils.backendLink.prohibitor.actions.*;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.function.Consumer;

public class ProhibitorCommands {

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> root = CommandManager.literal("prohibitor").requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2));

        root.then(CommandManager.literal("ban")
                .then(CommandManager.literal("perm")
                        .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                                .then(CommandManager.argument("reason", StringArgumentType.greedyString())
                                        .executes(context -> BanAction.permBan(players(context), reason(context), who(context), response(context)))))));

        root.then(CommandManager.literal("ban")
                .then(CommandManager.literal("skin")
                        .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                                .then(CommandManager.argument("reason", StringArgumentType.greedyString())
                                        .executes(context -> SkinBanAction.skinBan(players(context), reason(context), who(context), response(context)))))));

        root.then(CommandManager.literal("mute")
                .then(CommandManager.literal("perm")
                        .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                                .then(CommandManager.argument("reason", StringArgumentType.greedyString())
                                        .executes(context -> MuteAction.permMute(players(context), reason(context), who(context), response(context)))))));

        root.then(CommandManager.literal("whitelist")
                .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                        .then(CommandManager.argument("reason", StringArgumentType.greedyString())
                                .executes(context -> WhitelistAction.whitelist(players(context), reason(context), who(context), response(context))))));

        root.then(CommandManager.literal("kick")
                .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                        .then(CommandManager.argument("reason", StringArgumentType.greedyString())
                                .executes(context -> KickAction.kick(players(context), reason(context), who(context), response(context))))));

        root.then(CommandManager.literal("warn")
                .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                        .then(CommandManager.argument("reason", StringArgumentType.greedyString())
                                .executes(context -> WarnAction.warn(players(context), reason(context), who(context), response(context))))));

        root.then(CommandManager.literal("sus")
                .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                        .then(CommandManager.argument("reason", StringArgumentType.greedyString())
                                .executes(context -> SusAction.sus(players(context), reason(context), who(context), response(context))))));


        for (ChronoUnit unit : ChronoUnit.values()) {
            if (unit != ChronoUnit.MINUTES && unit != ChronoUnit.HOURS && unit != ChronoUnit.DAYS && unit != ChronoUnit.MONTHS && unit != ChronoUnit.YEARS)
                continue;

            root.then(CommandManager.literal("ban")
                    .then(CommandManager.literal("temp")
                            .then(CommandManager.literal(unit.name())
                                    .then(CommandManager.argument("duration", IntegerArgumentType.integer())
                                            .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                                                    .then(CommandManager.argument("reason", StringArgumentType.greedyString())
                                                            .executes(context -> BanAction.tempBan(players(context), reason(context), who(context), unit, duration(context), response(context)))))))));
            root.then(CommandManager.literal("mute")
                    .then(CommandManager.literal("temp")
                            .then(CommandManager.literal(unit.name())
                                    .then(CommandManager.argument("duration", IntegerArgumentType.integer())
                                            .then(CommandManager.argument("player", GameProfileArgumentType.gameProfile())
                                                    .then(CommandManager.argument("reason", StringArgumentType.greedyString())
                                                            .executes(context -> MuteAction.tempMute(players(context), reason(context), who(context), unit, duration(context), response(context)))))))));
        }

        root.then(CommandManager.literal("globalchat")
                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean enabled = BoolArgumentType.getBool(context, "enabled");
                            return ChatDisablerAction.chatDisable(Integrations.getSenderName(context.getSource()), enabled, text -> context.getSource().getServer().getPlayerManager().broadcast(text, false));
                        })));
        root.then(CommandManager.literal("linking")
                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                        .executes(context -> {
                            boolean enabled = BoolArgumentType.getBool(context, "enabled");
                            return LinkingDisablerAction.linkDisabler(Integrations.getSenderName(context.getSource()), enabled, text -> context.getSource().sendFeedback(() -> text, false));
                        })));

        root.then(CommandManager.literal("sendMissedMessages")
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(ctx ->{
                            ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
                            ProhibitorModule.sendMissedMessages(player);
                            return 1;
                        })));

        dispatcher.register(root);

        dispatcher.register(CommandManager.literal("discordLink").executes(context -> {
            ServerPlayerEntity player = context.getSource().getPlayer();
            if (player != null) {
                if (ProhibitorModule.config().linkingEnabled) {
                    String linkID = RandomStringUtils.randomAlphanumeric(5).toLowerCase();
                    boolean success = BackendServer.post("link/start/" + linkID + "/" + player.getUuidAsString() + "/" + BackendServer.config.bridgeName.replaceAll(" ", "%20"));
                    if (success) {
                        player.sendMessage(Text.literal("Linking your accounts allows you to send messages to the server."));
                        player.sendMessage(Text.literal("All rules apply, this role may be revoked if you break the rules."));
                        player.sendMessage(Text.empty());

                        player.sendMessage(Text.literal("How to link:"));
                        player.sendMessage(Text.literal("1. Join the ").append(Text.literal("Legacy Edition Mini Games Discord Server").styled(style -> urlStyle(style, "https://discord.gg/qv4sXegAv4"))));
                        player.sendMessage(Text.literal("2. Find the ").append(Text.literal("\"Verification\" channel").styled(style -> urlStyle(style, "https://discord.com/channels/860805393441357834/1082945943827128360"))));
                        player.sendMessage(Text.literal("3. Click on the Link button and enter the code below and wait"));
                        player.sendMessage(Text.literal(linkID).styled(style -> copyStyle(style, linkID)));


                    }
                } else {
                    player.sendMessage(Text.translatable("prohibitor.linking.disabled").formatted(Formatting.RED));
                }
            }
            return 1;
        }));

        dispatcher.register(CommandManager.literal("discordMSG").requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("msg", TextArgumentType.text())
                        .executes(context -> {
                            Text text = TextArgumentType.getTextArgument(context, "msg");
                            Broadcaster.broadcast(context.getSource().getServer(), text);
                            return 1;
                        })));

        dispatcher.register(CommandManager.literal("discordChatMSG").requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("msg", TextArgumentType.text())
                        .executes(context -> {
                            Text text = TextArgumentType.getTextArgument(context, "msg");
                            Broadcaster.broadcast(context.getSource().getServer(), text);
                            context.getSource().getServer().getPlayerManager().broadcast(Texts.parse(context.getSource(), text, null, 0), false);
                            return 1;
                        })));

    }

    private static Collection<GameProfile> players(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return GameProfileArgumentType.getProfileArgument(context, "player");
    }

    private static String who(CommandContext<ServerCommandSource> context) {
        return Integrations.getSenderName(context.getSource());
    }

    private static String reason(CommandContext<ServerCommandSource> context) {
        return StringArgumentType.getString(context, "reason");
    }

    private static int duration(CommandContext<ServerCommandSource> context) {
        return IntegerArgumentType.getInteger(context, "duration");
    }

    private static Consumer<Text> response(CommandContext<ServerCommandSource> context) {
        return (text) -> context.getSource().sendFeedback(() -> text, true);
    }

    public static JsonObject stamp(String reason, String who) {
        JsonObject obj = new JsonObject();
        obj.addProperty("stamp_who", who + " - Server");
        obj.addProperty("stamp_source", BackendServer.config.serverName + " - Server");
        obj.addProperty("stamp_reason", reason);
        return obj;
    }

    private static Style copyStyle(Style style, String text) {
        return style.withColor(Formatting.BLUE)
                .withUnderline(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, text))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to copy")));
    }

    private static Style urlStyle(Style style, String url) {
        return style.withColor(Formatting.BLUE)
                .withUnderline(true)
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to open")));
    }
}
