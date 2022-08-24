package net.kyrptonaught.serverutils.dimensionLoader;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.argument.CommandFunctionArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.FunctionCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class DimensionLoaderCommand {

    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher, boolean b) {
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = CommandManager.literal(DimensionLoaderMod.MOD_ID).requires(source -> source.hasPermissionLevel(2));

        literalArgumentBuilder.then(CommandManager.literal("prepareDimension")
                .then(CommandManager.argument("id", IdentifierArgumentType.identifier())
                        .then(CommandManager.argument("dimType", IdentifierArgumentType.identifier())
                                .then(CommandManager.argument("callbackFunction", CommandFunctionArgumentType.commandFunction())
                                        .suggests(FunctionCommand.SUGGESTION_PROVIDER)
                                        .executes(context -> executePrepare(context, getID(context, "id"), getID(context, "dimType"), CommandFunctionArgumentType.getFunctions(context, "callbackFunction"))))
                                .executes(context -> executePrepare(context, getID(context, "id"), getID(context, "dimType"), null)))));


        literalArgumentBuilder.then(CommandManager.literal("unload")
                .then(CommandManager.argument("id", IdentifierArgumentType.identifier())
                        .suggests(DimensionLoaderCommand::getLoadedSuggestions)
                        .then(CommandManager.argument("callbackFunction", CommandFunctionArgumentType.commandFunction())
                                .suggests(FunctionCommand.SUGGESTION_PROVIDER)
                                .executes(context -> executeUnload(context, getID(context, "id"), CommandFunctionArgumentType.getFunctions(context, "callbackFunction"))))
                        .executes(context -> executeUnload(context, getID(context, "id"), null))));

        GameRules.accept(new GameRules.Visitor() {
            @Override
            public <T extends GameRules.Rule<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                literalArgumentBuilder.then(CommandManager.literal("setGamerule")
                        .then(CommandManager.argument("id", IdentifierArgumentType.identifier())
                                .suggests(DimensionLoaderCommand::getLoadedSuggestions)
                                .then((CommandManager.literal(key.getName())
                                        .executes(context -> executeQuery(context.getSource(), getID(context, "id"), key)))
                                        .then(type.argument("value")
                                                .executes(context -> executeSet(context, getID(context, "id"), key))))));
            }
        });
        dispatcher.register(literalArgumentBuilder);
    }

    private static int executePrepare(CommandContext<ServerCommandSource> context, Identifier id, Identifier dimType, Collection<CommandFunction> functions) {
        context.getSource().sendFeedback(DimensionLoaderMod.loadDimension(context.getSource().getServer(), id, dimType, functions), false);
        return 1;
    }

    private static int executeUnload(CommandContext<ServerCommandSource> context, Identifier id, Collection<CommandFunction> functions) {
        context.getSource().sendFeedback(DimensionLoaderMod.unLoadDimension(context.getSource().getServer(), id, functions), false);
        return 1;
    }

    private static Identifier getID(CommandContext<ServerCommandSource> context, String id) {
        return IdentifierArgumentType.getIdentifier(context, id);
    }

    private static CompletableFuture<Suggestions> getLoadedSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        DimensionLoaderMod.loadedWorlds.keySet().forEach(identifier -> builder.suggest(identifier.toString()));
        return builder.buildFuture();
    }

    private static <T extends GameRules.Rule<T>> int executeSet(CommandContext<ServerCommandSource> context, Identifier id, GameRules.Key<T> key) {
        ServerCommandSource serverCommandSource = context.getSource();
        CustomDimHolder holder = DimensionLoaderMod.loadedWorlds.get(id);
        if (holder == null) {
            serverCommandSource.sendFeedback(new LiteralText("Dimension not found"), false);
            return 1;
        } else if (!holder.wasRegistered()) {
            serverCommandSource.sendFeedback(new LiteralText("Dimension not initialized"), false);
            return 1;
        }

        T rule = holder.world.asWorld().getGameRules().get(key);
        rule.set(context, "value");
        serverCommandSource.sendFeedback(new LiteralText("[" + id + "]: ").append(new TranslatableText("commands.gamerule.set", key.getName(), rule.toString())), true);
        return rule.getCommandResult();
    }

    private static <T extends GameRules.Rule<T>> int executeQuery(ServerCommandSource source, Identifier id, GameRules.Key<T> key) {
        CustomDimHolder holder = DimensionLoaderMod.loadedWorlds.get(id);
        if (holder == null) {
            source.sendFeedback(new LiteralText("Dimension not found"), false);
            return 1;
        } else if (!holder.wasRegistered()) {
            source.sendFeedback(new LiteralText("Dimension not initialized"), false);
            return 1;
        }

        T rule = holder.world.asWorld().getGameRules().get(key);
        source.sendFeedback(new LiteralText("[" + id + "]: ").append(new TranslatableText("commands.gamerule.query", key.getName(), rule.toString())), false);
        return rule.getCommandResult();
    }
}
