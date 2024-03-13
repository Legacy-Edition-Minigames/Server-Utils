package net.kyrptonaught.serverutils.customMapLoader;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.kyrptonaught.serverutils.customMapLoader.voting.HostOptions;
import net.kyrptonaught.serverutils.customMapLoader.voting.Votebook;
import net.kyrptonaught.serverutils.customMapLoader.voting.Voter;
import net.kyrptonaught.serverutils.userConfig.UserConfigStorage;
import net.minecraft.command.argument.CommandFunctionArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.FunctionCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.List;

public class CustomMapLoaderCommands {

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> cmd = CommandManager.literal("custommaploader").requires((source) -> source.hasPermissionLevel(2));

        cmd.then(CommandManager.literal("voting")
                .then(CommandManager.literal("openBook")
                        .executes(context -> {
                            Votebook.generateBookLibrary(CustomMapLoaderMod.BATTLE_MAPS.values().stream().toList());
                            Votebook.getPage(context.getSource().getPlayer(), "title", null).open();
                            return 1;
                        }))
                .then(CommandManager.literal("giveBook")
                        .executes(context -> {
                            //Votebook.generateBookLibrary(CustomMapLoaderMod.BATTLE_MAPS.values().stream().toList());
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            player.giveItemStack(Votebook.getPage(player, "title", null).getBook());
                            return 1;
                        }))
                .then(CommandManager.literal("showBookPage")
                        .then(CommandManager.argument("page", StringArgumentType.string())
                                .executes(context -> {
                                    String page = StringArgumentType.getString(context, "page");

                                    Votebook.getPage(context.getSource().getPlayer(), page, null).open();
                                    return 1;
                                })
                                .then(CommandManager.literal("arg")
                                        .then(CommandManager.argument("arg", StringArgumentType.string())
                                                .executes(context -> {
                                                    String page = StringArgumentType.getString(context, "page");
                                                    String arg = StringArgumentType.getString(context, "arg");

                                                    Votebook.getPage(context.getSource().getPlayer(), page, arg).open();
                                                    return 1;
                                                })
                                                .then(CommandManager.literal("after")
                                                        .fork(dispatcher.getRoot(), context -> {
                                                            context.getChild().getCommand().run(context.getChild());

                                                            String page = StringArgumentType.getString(context, "page");
                                                            String arg = StringArgumentType.getString(context, "arg");
                                                            Votebook.getPage(context.getSource().getPlayer(), page, arg).open();

                                                            return List.of();
                                                        }))))
                                .then(CommandManager.literal("after")
                                        .fork(dispatcher.getRoot(), (context -> {
                                            context.getChild().getCommand().run(context.getChild());

                                            String page = StringArgumentType.getString(context, "page");
                                            Votebook.getPage(context.getSource().getPlayer(), page, null).open();

                                            return List.of();
                                        })))))
                .then(CommandManager.literal("vote")
                        .then(CommandManager.argument("map", IdentifierArgumentType.identifier())
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    Identifier map = IdentifierArgumentType.getIdentifier(context, "map");

                                    Voter.voteFor(context.getSource().getServer(), player, map);
                                    return 1;
                                })))
                .then(CommandManager.literal("removeVote")
                        .executes(context -> {
                            Voter.removeVote(context.getSource().getServer(), context.getSource().getPlayer());
                            return 1;
                        }))
                .then(CommandManager.literal("start")
                        .executes(context -> {
                            Voter.prepVote(context.getSource().getServer(), CustomMapLoaderMod.BATTLE_MAPS.values().stream().toList());
                            return 1;
                        }))
                .then(CommandManager.literal("end")
                        .then(CommandManager.argument("dimID", IdentifierArgumentType.identifier())
                                .then(CommandManager.argument("centralSpawnEnabled", BoolArgumentType.bool())
                                        .then(CommandManager.argument("players", EntityArgumentType.players())
                                                .then(CommandManager.argument("callbackFunction", CommandFunctionArgumentType.commandFunction())
                                                        .suggests(FunctionCommand.SUGGESTION_PROVIDER)
                                                        .executes(context -> {
                                                            Identifier id = IdentifierArgumentType.getIdentifier(context, "dimID");
                                                            boolean centralSpawnEnabled = BoolArgumentType.getBool(context, "centralSpawnEnabled");
                                                            Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");

                                                            Identifier winner = Voter.endVote(context.getSource().getServer());
                                                            CustomMapLoaderMod.battleLoad(context.getSource().getServer(), winner, id, centralSpawnEnabled, players, CommandFunctionArgumentType.getFunctions(context, "callbackFunction"));
                                                            return 1;
                                                        }))
                                                .executes(context -> {
                                                    Identifier id = IdentifierArgumentType.getIdentifier(context, "dimID");
                                                    boolean centralSpawnEnabled = BoolArgumentType.getBool(context, "centralSpawnEnabled");
                                                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");

                                                    Identifier winner = Voter.endVote(context.getSource().getServer());
                                                    CustomMapLoaderMod.battleLoad(context.getSource().getServer(), winner, id, centralSpawnEnabled, players, null);
                                                    return 1;
                                                }))))));

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

        cmd.then(CommandManager.literal("hostOptions")
                .then(CommandManager.literal("enableMap")
                        .then(CommandManager.argument("mapID", IdentifierArgumentType.identifier())
                                .then(CommandManager.argument("enabled", BoolArgumentType.bool())
                                        .executes(context -> {
                                            Identifier id = IdentifierArgumentType.getIdentifier(context, "mapID");
                                            boolean enabled = BoolArgumentType.getBool(context, "enabled");

                                            HostOptions.enableDisableMap(context.getSource().getServer(), id, enabled);
                                            return 1;
                                        })))));

        cmd.then(CommandManager.literal("playerOptions")
                .then(CommandManager.literal("optionalPacks")
                        .then(CommandManager.argument("mapID", IdentifierArgumentType.identifier())
                                .then(CommandManager.literal("accept")
                                        .then(CommandManager.argument("packID", IdentifierArgumentType.identifier())
                                                .then(CommandManager.argument("accepted", BoolArgumentType.bool())
                                                        .executes(context -> {
                                                            ServerPlayerEntity player = context.getSource().getPlayer();
                                                            Identifier mapID = IdentifierArgumentType.getIdentifier(context, "mapID");
                                                            Identifier packID = IdentifierArgumentType.getIdentifier(context, "packID");
                                                            boolean accepted = BoolArgumentType.getBool(context, "accepted");

                                                            UserConfigStorage.setValue(player, HostOptions.getMapResourcePackKey(mapID, packID), String.valueOf(accepted));
                                                            UserConfigStorage.syncPlayer(player);

                                                            //Votebook.generateOptionalPack(context.getSource().getPlayer(), CustomMapLoaderMod.BATTLE_MAPS.get(mapID)).open();
                                                            return 1;
                                                        }))))
                                .then(CommandManager.literal("showPrompt")
                                        .executes(context -> {
                                            Identifier mapID = IdentifierArgumentType.getIdentifier(context, "mapID");
                                            //Votebook.generateOptionalPack(context.getSource().getPlayer(), CustomMapLoaderMod.BATTLE_MAPS.get(mapID)).open();
                                            return 1;
                                        })))
                        .then(CommandManager.literal("policy")
                                .then(CommandManager.argument("policy", StringArgumentType.string())
                                        .executes(context -> {
                                            ServerPlayerEntity player = context.getSource().getPlayer();
                                            String policy = StringArgumentType.getString(context, "policy");

                                            UserConfigStorage.setValue(player, HostOptions.getPromptKey(), policy);
                                            UserConfigStorage.syncPlayer(player);
                                            return 1;
                                        }))
                                .then(CommandManager.argument("mapID", IdentifierArgumentType.identifier())
                                        .then(CommandManager.argument("dontAsk", BoolArgumentType.bool())
                                                .executes(context -> {
                                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                                    Identifier mapID = IdentifierArgumentType.getIdentifier(context, "mapID");
                                                    boolean dontAsk = BoolArgumentType.getBool(context, "dontAsk");

                                                    UserConfigStorage.setValue(player, HostOptions.getPromptKey(mapID), String.valueOf(dontAsk));
                                                    UserConfigStorage.syncPlayer(player);

                                                    //Votebook.generateOptionalPack(context.getSource().getPlayer(), CustomMapLoaderMod.BATTLE_MAPS.get(mapID)).open();
                                                    return 1;
                                                }))))
                        .then(CommandManager.literal("reset")
                                .executes(context -> {
                                    context.getSource().sendMessage(Text.literal("no worky yet"));
                                    return 1;
                                }))));

        cmd.then(CommandManager.literal("battle")
                .then(CommandManager.literal("tp")
                        .then(CommandManager.argument("dimID", IdentifierArgumentType.identifier())
                                .then(CommandManager.argument("initialSpawn", BoolArgumentType.bool())
                                        .then(CommandManager.argument("players", EntityArgumentType.players())
                                                .executes(context -> {
                                                    Identifier id = IdentifierArgumentType.getIdentifier(context, "dimID");
                                                    boolean initial = BoolArgumentType.getBool(context, "initialSpawn");
                                                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
                                                    CustomMapLoaderMod.battleTp(id, initial, players);
                                                    return 1;
                                                })))))
                .then(CommandManager.literal("unload")
                        .then(CommandManager.argument("dimID", IdentifierArgumentType.identifier())
                                .then(CommandManager.argument("callbackFunction", CommandFunctionArgumentType.commandFunction())
                                        .suggests(FunctionCommand.SUGGESTION_PROVIDER)
                                        .executes(context -> {
                                            Identifier id = IdentifierArgumentType.getIdentifier(context, "dimID");
                                            CustomMapLoaderMod.unloadBattleMap(context.getSource().getServer(), id, CommandFunctionArgumentType.getFunctions(context, "callbackFunction"));
                                            return 1;
                                        }))
                                .executes(context -> {
                                    Identifier id = IdentifierArgumentType.getIdentifier(context, "dimID");
                                    CustomMapLoaderMod.unloadBattleMap(context.getSource().getServer(), id, null);
                                    return 1;
                                }))));

        cmd.then(CommandManager.literal("lobby")
                .then(CommandManager.literal("load")
                        .then(CommandManager.argument("lobby", IdentifierArgumentType.identifier())
                                .then(CommandManager.argument("dimID", IdentifierArgumentType.identifier())
                                        .then(CommandManager.argument("players", EntityArgumentType.players())
                                                .then(CommandManager.argument("callbackFunction", CommandFunctionArgumentType.commandFunction())
                                                        .suggests(FunctionCommand.SUGGESTION_PROVIDER)
                                                        .executes(context -> {
                                                            Identifier id = IdentifierArgumentType.getIdentifier(context, "dimID");
                                                            Identifier lobbyID = IdentifierArgumentType.getIdentifier(context, "lobby");
                                                            Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
                                                            CustomMapLoaderMod.prepareLobby(context.getSource().getServer(), lobbyID, id, players, CommandFunctionArgumentType.getFunctions(context, "callbackFunction"));
                                                            return 1;
                                                        }))))))
                .then(CommandManager.literal("unload")
                        .then(CommandManager.argument("dimID", IdentifierArgumentType.identifier())
                                .then(CommandManager.argument("callbackFunction", CommandFunctionArgumentType.commandFunction())
                                        .suggests(FunctionCommand.SUGGESTION_PROVIDER)
                                        .executes(context -> {
                                            Identifier id = IdentifierArgumentType.getIdentifier(context, "dimID");
                                            CustomMapLoaderMod.unloadLobbyMap(context.getSource().getServer(), id, CommandFunctionArgumentType.getFunctions(context, "callbackFunction"));
                                            return 1;
                                        }))
                                .executes(context -> {
                                    Identifier id = IdentifierArgumentType.getIdentifier(context, "dimID");
                                    CustomMapLoaderMod.unloadLobbyMap(context.getSource().getServer(), id, null);
                                    return 1;
                                })))
                .then(CommandManager.literal("tp")
                        .then(CommandManager.argument("dimID", IdentifierArgumentType.identifier())
                                .then(CommandManager.argument("players", EntityArgumentType.players())
                                        .then(CommandManager.argument("winner", EntityArgumentType.player())
                                                .executes(context -> {
                                                    Identifier id = IdentifierArgumentType.getIdentifier(context, "dimID");
                                                    Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
                                                    ServerPlayerEntity winner = EntityArgumentType.getPlayer(context, "winner");

                                                    CustomMapLoaderMod.teleportToLobby(id, players, winner);
                                                    return 1;
                                                }))
                                        .executes(context -> {
                                            Identifier id = IdentifierArgumentType.getIdentifier(context, "dimID");
                                            Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");

                                            CustomMapLoaderMod.teleportToLobby(id, players, null);
                                            return 1;
                                        })))));
        dispatcher.register(cmd);
    }
}
