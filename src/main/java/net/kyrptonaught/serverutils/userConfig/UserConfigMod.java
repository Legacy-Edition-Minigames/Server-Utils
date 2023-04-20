package net.kyrptonaught.serverutils.userConfig;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyrptonaught.serverutils.Module;
import net.minecraft.command.argument.CommandFunctionArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
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
        LESS, GREATER, LESS_EQUAL, GREATER_EQUAL, EQUAL, NOT_EQUAl;
    }

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> UserConfigStorage.loadPlayer(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> UserConfigStorage.unloadPlayer(handler.player));
    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        super.registerCommands(dispatcher);

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
                                                        Collection<CommandFunction> functions = CommandFunctionArgumentType.getFunctions(context, "function");
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
                            context.getSource().sendFeedback(player.getDisplayName().copy().append(" : ").append(Text.literal(setValue)), false);
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
                                int setValue = Integer.parseInt(UserConfigStorage.getValue(player, configID));
                                scoreboard.getPlayerScore(player.getEntityName(), obj).setScore(setValue);
                            }
                            return 1;
                        })));
        baseNode.then(copyCMDNode);

        dispatcher.register(CommandManager.literal("userconfig").requires((source) -> source.hasPermissionLevel(2)).then(baseNode));
    }

    public static boolean evaluate(EQUATION_TYPE type, String obj1, String obj2) {
        if (obj1 == null || obj2 == null) return false;
        int compares = obj1.compareTo(obj2);
        return switch (type) {
            case EQUAL -> compares == 0;
            case NOT_EQUAl -> compares != 0;
            case LESS -> compares < 0;
            case GREATER -> compares > 0;
            case LESS_EQUAL -> compares <= 0;
            case GREATER_EQUAL -> compares >= 0;
        };
    }
}