package net.kyrptonaught.serverutils.customMapLoader;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;

public class CustomMapLoaderCommands {

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> cmd = CommandManager.literal("custommaploader").requires((source) -> source.hasPermissionLevel(2));

        cmd.then(CommandManager.literal("voting")
                .then(CommandManager.literal("openBook")
                        .executes(context -> {
                            Votebook.generateBookLibrary(CustomMapLoaderMod.loadedMaps.values().stream().toList());
                            Votebook.openbook(context.getSource().getPlayer(), "title");
                            return 1;
                        }))
                .then(CommandManager.literal("giveBook")
                        .executes(context -> {
                            Votebook.generateBookLibrary(CustomMapLoaderMod.loadedMaps.values().stream().toList());
                            Votebook.giveBook(context.getSource().getPlayer(), "title");
                            return 1;
                        }))
                .then(CommandManager.literal("showBookPage")
                        .then(CommandManager.argument("page", StringArgumentType.word())
                                .executes(context -> {
                                    String page = StringArgumentType.getString(context, "page");

                                    Votebook.openbook(context.getSource().getPlayer(), page);
                                    return 1;
                                })
                                .then(CommandManager.literal("after")
                                        .fork(dispatcher.getRoot(), (context -> {
                                            context.getChild().getCommand().run(context);

                                            Votebook.generateBookLibrary(CustomMapLoaderMod.loadedMaps.values().stream().toList());
                                            String page = StringArgumentType.getString(context, "page");
                                            Votebook.openbook(context.getSource().getPlayer(), page);

                                            return List.of();
                                        })))))
                .then(CommandManager.literal("vote")
                        .then(CommandManager.argument("map", StringArgumentType.word())
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    String map = StringArgumentType.getString(context, "map");

                                    Voter.voteFor(context.getSource().getServer(), player, map, CustomMapLoaderMod.loadedMaps.get(map).getName());
                                    return 1;
                                })))
                .then(CommandManager.literal("removeVote")
                        .executes(context -> {
                            Voter.removeVote(context.getSource().getServer(), context.getSource().getPlayer());
                            return 1;
                        }))
                .then(CommandManager.literal("start")
                        .executes(context -> {
                            Voter.prepVote(context.getSource().getServer(), CustomMapLoaderMod.loadedMaps.values().stream().toList());
                            return 1;
                        }))
                .then(CommandManager.literal("end")
                        .then(CommandManager.argument("dimID", IdentifierArgumentType.identifier())
                                .executes(context -> {
                                    Identifier id = IdentifierArgumentType.getIdentifier(context, "dimID");
                                    CustomMapLoaderMod.prepareLemmod(context.getSource().getServer(), id);
                                    return 1;
                                }))));

        for (MapSize mapSize : MapSize.values()) {
            cmd.then(CommandManager.literal("hostOptions")
                    .then(CommandManager.literal("mapSize")
                            .then(CommandManager.literal("set")
                                    .then(CommandManager.literal(mapSize.id)
                                            .executes(context -> {
                                                HostOptions.selectedMapSize = mapSize;
                                                return 1;
                                            })))));
        }

        dispatcher.register(cmd);
    }
}
