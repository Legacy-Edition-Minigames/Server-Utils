package net.kyrptonaught.serverutils.dimensionLoader;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kyrptonaught.serverutils.CMDHelper;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.command.argument.CommandFunctionArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.FunctionCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class DimensionLoaderCommand {

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = CommandManager.literal(ServerUtilsMod.DimensionLoaderModule.getMOD_ID()).requires(source -> source.hasPermissionLevel(2));

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

        dispatcher.register(CommandManager.literal("whereami").executes(context -> {
            context.getSource().sendFeedback(()->DimensionLoaderMod.whereAmI(context.getSource().getPlayer()), false);
            return 1;
        }));

        dispatcher.register(literalArgumentBuilder);
    }

    private static int executePrepare(CommandContext<ServerCommandSource> context, Identifier id, Identifier dimType, Collection<CommandFunction> functions) {
        context.getSource().sendFeedback(()->DimensionLoaderMod.loadDimension(context.getSource().getServer(), id, dimType, functions), false);
        return 1;
    }

    private static int executeUnload(CommandContext<ServerCommandSource> context, Identifier id, Collection<CommandFunction> functions) {
        context.getSource().sendFeedback(()->DimensionLoaderMod.unLoadDimension(context.getSource().getServer(), id, functions), false);
        return 1;
    }

    private static Identifier getID(CommandContext<ServerCommandSource> context, String id) {
        return IdentifierArgumentType.getIdentifier(context, id);
    }

    private static CompletableFuture<Suggestions> getLoadedSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        DimensionLoaderMod.loadedWorlds.keySet().forEach(identifier -> builder.suggest(identifier.toString()));
        return builder.buildFuture();
    }
}
