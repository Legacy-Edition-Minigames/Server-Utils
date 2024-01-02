package net.kyrptonaught.serverutils.utilityCommands;

import com.mojang.brigadier.CommandDispatcher;
import net.kyrptonaught.serverutils.Module;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.CommandFunctionArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.FunctionCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collection;

public class UtilCommandsMod extends Module {

    @Override
    public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("ifop")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("player", EntityArgumentType.player())
                        .then(CommandManager.literal("runFunction")
                                .then(CommandManager.argument("function", CommandFunctionArgumentType.commandFunction())
                                        .suggests(FunctionCommand.SUGGESTION_PROVIDER)
                                        .executes(context -> {
                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                            MinecraftServer server = context.getSource().getServer();
                                            Collection<CommandFunction> functions = CommandFunctionArgumentType.getFunctions(context, "function");

                                            if (server.getPlayerManager().isOperator(player.getGameProfile()))
                                                for (CommandFunction commandFunction : functions)
                                                    server.getCommandFunctionManager().execute(commandFunction, player.getCommandSource().withLevel(2).withSilent());

                                            return 1;
                                        })))
                        .then(CommandManager.literal("runCommand")
                                .fork(dispatcher.getRoot(), (context -> {
                                    ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                    MinecraftServer server = context.getSource().getServer();

                                    ArrayList<ServerCommandSource> list = new ArrayList<>();
                                    if (server.getPlayerManager().isOperator(player.getGameProfile()))
                                        list.add(player.getCommandSource().withLevel(2).withSilent());

                                    return list;
                                }))
                        )));

        dispatcher.register(CommandManager.literal("forcetp")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("player", EntityArgumentType.players())
                        .then(CommandManager.argument("pos", BlockPosArgumentType.blockPos())
                                .executes(context -> {
                                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "player");
                                    BlockPos pos = BlockPosArgumentType.getBlockPos(context, "pos");

                                    context.getSource().getServer().execute(() -> {
                                        players.forEach(player -> player.teleport(pos.getX(), pos.getY(), pos.getZ()));
                                    });

                                    return 1;
                                }))));

    }
}
