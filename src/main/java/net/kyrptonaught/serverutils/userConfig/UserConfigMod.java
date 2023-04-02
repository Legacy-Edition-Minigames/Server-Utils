package net.kyrptonaught.serverutils.userConfig;

import com.mojang.brigadier.CommandDispatcher;
import net.kyrptonaught.serverutils.Module;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class UserConfigMod extends Module {
    private static final DataType<?>[] dataTypes = new DataType[]{new DataType.IntType(), new DataType.StringType(), new DataType.BoolType()};

    enum EQUATIONTYPE {
        LESS(true), GREATER(true),
        LESS_EQUAL(true), GREATER_EQUAL(true),
        EQUAL, NOT_EQUAl;

        final boolean isInequality;

        EQUATIONTYPE(boolean isInequality) {
            this.isInequality = isInequality;
        }

        EQUATIONTYPE() {
            this(false);
        }

        <T> boolean evaluate(DataType<T> datatype, T obj1, T obj2) {
            int compares = datatype.compare(obj1, obj2);
            return switch (this) {
                case EQUAL -> compares == 0;
                case NOT_EQUAl -> compares != 0;
                case LESS -> compares < 0;
                case GREATER -> compares > 0;
                case LESS_EQUAL -> compares <= 0;
                case GREATER_EQUAL -> compares >= 0;
            };
        }
    }

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        super.registerCommands(dispatcher);

        var baseNode = CommandManager.argument("player", EntityArgumentType.player());

        var testCMDNode = CommandManager.literal("test");
        for (DataType<?> type : dataTypes) {
            for (EQUATIONTYPE equationtype : EQUATIONTYPE.values()) {
                if (equationtype.isInequality && !type.canCompareInequalities()) continue;
                testCMDNode.then(CommandManager.literal(type.cmdName)
                        .then(CommandManager.argument("configID", IdentifierArgumentType.identifier())
                                .then(CommandManager.literal(equationtype.name())
                                        .then(CommandManager.argument("testValue", type.getArgumentType())
                                                .executes(context -> {
                                                    Identifier configID = IdentifierArgumentType.getIdentifier(context, "configID");
                                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");

                                                    Object testValue = type.getArgument(context, "testValue");
                                                    Object setValue = "test";


                                                    //System.out.println(equationtype.evaluate(type, setValue, testValue));
                                                    return 1;
                                                })))));
            }
        }
        baseNode.then(testCMDNode);

        var setCMDNode = CommandManager.literal("set");
        for (DataType<?> type : dataTypes) {
            setCMDNode.then(CommandManager.literal(type.cmdName)
                    .then(CommandManager.argument("configID", IdentifierArgumentType.identifier())
                            .then(CommandManager.argument("value", type.getArgumentType())
                                    .executes(context -> {
                                        Identifier configID = IdentifierArgumentType.getIdentifier(context, "configID");
                                        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                        Object value = type.getArgument(context, "value");
                                        UserConfigStorage.setValue(player, configID, value);
                                        return 1;
                                    }))));
        }
        baseNode.then(setCMDNode);

        var getCMDNode = CommandManager.literal("get");
        for (DataType<?> type : dataTypes) {
            getCMDNode.then(CommandManager.literal(type.cmdName)
                    .then(CommandManager.argument("configID", IdentifierArgumentType.identifier())
                            .executes(context -> {
                                Identifier configID = IdentifierArgumentType.getIdentifier(context, "configID");
                                ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                String setValue = UserConfigStorage.getValue(player, configID);
                                context.getSource().sendFeedback(Text.literal(setValue), false);
                                return 1;
                            })));
        }
        baseNode.then(getCMDNode);

        var copyCMDNode = CommandManager.literal("copy");
        copyCMDNode.then(CommandManager.literal("INT")
                .then(CommandManager.argument("configID", IdentifierArgumentType.identifier())
                        .then(CommandManager.argument("scoreboard", ScoreboardObjectiveArgumentType.scoreboardObjective())
                                .executes(context -> {
                                    Identifier configID = IdentifierArgumentType.getIdentifier(context, "configID");
                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                    ScoreboardObjective obj = ScoreboardObjectiveArgumentType.getObjective(context, "scoreboard");

                                    int setValue = Integer.parseInt(UserConfigStorage.getValue(player, configID));

                                    ServerScoreboard scoreboard = context.getSource().getServer().getScoreboard();
                                    scoreboard.getPlayerScore(player.getEntityName(), obj).setScore(setValue);
                                    return 1;
                                }))));

        baseNode.then(copyCMDNode);

        dispatcher.register(CommandManager.literal("userconfig").requires((source) -> source.hasPermissionLevel(2)).then(baseNode));
    }
}
