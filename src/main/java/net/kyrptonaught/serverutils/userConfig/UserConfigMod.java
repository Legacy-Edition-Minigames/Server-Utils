package net.kyrptonaught.serverutils.userConfig;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyrptonaught.serverutils.Module;
import net.minecraft.command.argument.CommandFunctionArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.FunctionCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collection;

public class UserConfigMod extends Module {
    private enum EQUATION_TYPE {
        LESS, GREATER, LESS_EQUAL, GREATER_EQUAL, EQUAL, NOT_EQUAL
    }

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> UserConfigStorage.loadPlayer(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> UserConfigStorage.unloadPlayer(handler.player));
    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        var baseNode = CommandManager.argument("player", EntityArgumentType.players());

        var testCMDNode = CommandManager.literal("test");
        for (EQUATION_TYPE equationtype : EQUATION_TYPE.values()) {

            testCMDNode.then(CommandManager.argument("configID", IdentifierArgumentType.identifier())
                    .then(CommandManager.literal(equationtype.name())
                            .then(CommandManager.argument("testValue", StringArgumentType.string())
                                    .then(CommandManager.literal("runFunction")
                                            .then(CommandManager.argument("function", CommandFunctionArgumentType.commandFunction())
                                                    .suggests(FunctionCommand.SUGGESTION_PROVIDER)
                                                    .executes(context -> {
                                                        Identifier configID = IdentifierArgumentType.getIdentifier(context, "configID");
                                                        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
                                                        MinecraftServer server = context.getSource().getServer();
                                                        Collection<CommandFunction<ServerCommandSource>> functions = CommandFunctionArgumentType.getFunctions(context, "function");
                                                        String testValue = StringArgumentType.getString(context, "testValue");

                                                        for (ServerPlayerEntity player : players) {
                                                            if (evaluate(equationtype, UserConfigStorage.getValue(player, configID), testValue)) {
                                                                for (CommandFunction commandFunction : functions) {
                                                                    server.getCommandFunctionManager().execute(commandFunction, player.getCommandSource().withLevel(2).withSilent());
                                                                }
                                                            }
                                                        }
                                                        return 1;
                                                    })))
                                    .then(CommandManager.literal("runCommand")
                                            .fork(dispatcher.getRoot(), (context -> {
                                                Identifier configID = IdentifierArgumentType.getIdentifier(context, "configID");
                                                Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
                                                String testValue = StringArgumentType.getString(context, "testValue");

                                                ArrayList<ServerCommandSource> list = new ArrayList<>();
                                                for (ServerPlayerEntity player : players)
                                                    if (evaluate(equationtype, UserConfigStorage.getValue(player, configID), testValue))
                                                        list.add(player.getCommandSource().withLevel(2).withSilent());

                                                return list;
                                            }))
                                    ))));
        }
        baseNode.then(testCMDNode);

        var setCMDNode = CommandManager.literal("set");
        setCMDNode.then(CommandManager.argument("configID", IdentifierArgumentType.identifier())
                .then(CommandManager.argument("value", StringArgumentType.greedyString())
                        .executes(context -> {
                            Identifier configID = IdentifierArgumentType.getIdentifier(context, "configID");
                            Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
                            String value = StringArgumentType.getString(context, "value");

                            for (ServerPlayerEntity player : players)
                                UserConfigStorage.setValue(player, configID, value);
                            return 1;
                        })));
        baseNode.then(setCMDNode);

        var getCMDNode = CommandManager.literal("get");
        getCMDNode.then(CommandManager.argument("configID", IdentifierArgumentType.identifier())
                .executes(context -> {
                    Identifier configID = IdentifierArgumentType.getIdentifier(context, "configID");
                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
                    for (ServerPlayerEntity player : players) {
                        String setValue = UserConfigStorage.getValue(player, configID);
                        if (setValue != null)
                            context.getSource().sendFeedback(() -> player.getDisplayName().copy().append(" : ").append(Text.literal(setValue)), false);
                    }
                    return 1;
                }));
        baseNode.then(getCMDNode);

        var removeCMDNode = CommandManager.literal("remove");
        removeCMDNode.then(CommandManager.argument("configID", IdentifierArgumentType.identifier())
                .executes(context -> {
                    Identifier configID = IdentifierArgumentType.getIdentifier(context, "configID");
                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");

                    for (ServerPlayerEntity player : players)
                        UserConfigStorage.removeValue(player, configID);
                    return 1;
                }));
        baseNode.then(removeCMDNode);

        var copyCMDNode = CommandManager.literal("copy");
        copyCMDNode.then(CommandManager.argument("configID", IdentifierArgumentType.identifier())
                .then(CommandManager.argument("scoreboard", ScoreboardObjectiveArgumentType.scoreboardObjective())
                        .executes(context -> {
                            Identifier configID = IdentifierArgumentType.getIdentifier(context, "configID");
                            Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
                            ScoreboardObjective obj = ScoreboardObjectiveArgumentType.getObjective(context, "scoreboard");
                            ServerScoreboard scoreboard = context.getSource().getServer().getScoreboard();

                            for (ServerPlayerEntity player : players) {
                                try {
                                    int setValue = Integer.parseInt(UserConfigStorage.getValue(player, configID));
                                    scoreboard.getOrCreateScore(ScoreHolder.fromName(player.getNameForScoreboard()), obj).setScore(setValue);
                                } catch (NumberFormatException ignored) {
                                }
                            }
                            return 1;
                        })));
        baseNode.then(copyCMDNode);

        var cloneCMDNode = CommandManager.literal("clone");
        cloneCMDNode.then(CommandManager.argument("configID", IdentifierArgumentType.identifier())
                .then(CommandManager.argument("configID2", IdentifierArgumentType.identifier())
                        .executes(context -> {
                            Identifier configID = IdentifierArgumentType.getIdentifier(context, "configID");
                            Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
                            Identifier configID2 = IdentifierArgumentType.getIdentifier(context, "configID2");

                            for (ServerPlayerEntity player : players) {
                                String value = UserConfigStorage.getValue(player, configID);
                                UserConfigStorage.setValue(player, configID2, value);
                            }
                            return 1;
                        })));
        baseNode.then(cloneCMDNode);

        baseNode.then(CommandManager.literal("sync").executes(context -> {
            Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");

            for (ServerPlayerEntity player : players) {
                UserConfigStorage.syncPlayer(player);
            }
            return 1;
        }));

        dispatcher.register(CommandManager.literal("userconfig").requires((source) -> source.hasPermissionLevel(2)).then(baseNode));

        dispatcher.register(CommandManager.literal("userconfiggroup").requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("ADD")
                        .then(CommandManager.argument("groupID", IdentifierArgumentType.identifier())
                                .then(CommandManager.argument("configID", IdentifierArgumentType.identifier())
                                        .executes(context -> {
                                            Identifier groupID = IdentifierArgumentType.getIdentifier(context, "groupID");
                                            Identifier configID = IdentifierArgumentType.getIdentifier(context, "configID");
                                            UserConfigStorage.addToGroup(groupID, configID);
                                            return 1;
                                        }))))
                .then(CommandManager.literal("CLEAR")
                        .then(CommandManager.argument("groupID", IdentifierArgumentType.identifier())
                                .executes(context -> {
                                    Identifier groupID = IdentifierArgumentType.getIdentifier(context, "groupID");
                                    UserConfigStorage.removeGroup(groupID);
                                    return 1;
                                })))
                .then(CommandManager.literal("SAVE")
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .then(CommandManager.argument("groupID", IdentifierArgumentType.identifier())
                                        .then(CommandManager.argument("presetID", IdentifierArgumentType.identifier())
                                                .executes(context -> {
                                                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
                                                    Identifier groupID = IdentifierArgumentType.getIdentifier(context, "groupID");
                                                    Identifier presetID = IdentifierArgumentType.getIdentifier(context, "presetID");

                                                    for (ServerPlayerEntity player : players)
                                                        UserConfigStorage.saveGroupToPreset(player, groupID, presetID);
                                                    return 1;
                                                })))))
                .then(CommandManager.literal("LOAD")
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .then(CommandManager.argument("groupID", IdentifierArgumentType.identifier())
                                        .then(CommandManager.argument("presetID", IdentifierArgumentType.identifier())
                                                .executes(context -> {
                                                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
                                                    Identifier groupID = IdentifierArgumentType.getIdentifier(context, "groupID");
                                                    Identifier presetID = IdentifierArgumentType.getIdentifier(context, "presetID");

                                                    for (ServerPlayerEntity player : players)
                                                        UserConfigStorage.loadGroupFromPreset(player, groupID, presetID);
                                                    return 1;
                                                }))))));
    }

    private static boolean evaluate(EQUATION_TYPE type, String setValue, String testValue) {
        if (type == EQUATION_TYPE.EQUAL) {
            if ("_ANYTHING_".equals(testValue)) return setValue != null;
            if ("_NOTHING_".equals(testValue)) return setValue == null;
        }
        if (type == EQUATION_TYPE.NOT_EQUAL) {
            if ("_ANYTHING_".equals(testValue)) return setValue == null;
            if ("_NOTHING_".equals(testValue)) return setValue != null;
        }

        if (setValue == null || testValue == null) return false;
        int compares = compare(setValue, testValue);
        return switch (type) {
            case EQUAL -> compares == 0;
            case NOT_EQUAL -> compares != 0;
            case LESS -> compares < 0;
            case GREATER -> compares > 0;
            case LESS_EQUAL -> compares <= 0;
            case GREATER_EQUAL -> compares >= 0;
        };
    }

    private static int compare(String obj1, String obj2) {
        try {
            return Integer.compare(Integer.parseInt(obj1), Integer.parseInt(obj2));
        } catch (Exception ignored) {

        }
        return obj1.compareTo(obj2);
    }
}