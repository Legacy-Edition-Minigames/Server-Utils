package net.kyrptonaught.serverutils.panoramaViewer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Collections;

import static net.kyrptonaught.serverutils.ServerUtilsMod.PanoramaViewerModule;

public class PanoramaCommand {

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> cmd = CommandManager.literal("panorama")
                .requires((source) -> source.hasPermissionLevel(2));

        LiteralArgumentBuilder<ServerCommandSource> startCmd = CommandManager.literal("start");
        for (PanoramaConfig.AutoPanoramaEntry panoramaEntry : PanoramaViewerModule.getConfig().autoEntries) {
            startCmd.then(CommandManager.literal(panoramaEntry.panoramaName)
                    .then(CommandManager.argument("uiscaleOBJ", ScoreboardObjectiveArgumentType.scoreboardObjective())
                            .then(CommandManager.argument("isNight", BoolArgumentType.bool())
                                    .then(CommandManager.argument("player", EntityArgumentType.players())
                                            .executes(commandContext -> getScoreBoard(commandContext, panoramaEntry.panoramaName, EntityArgumentType.getPlayers(commandContext, "player"))))
                                    .executes(commandContext -> getScoreBoard(commandContext, panoramaEntry.panoramaName, Collections.singleton(commandContext.getSource().getPlayer()))))));

        }

        for (Panorama panorama : PanoramaViewer.panoramaEntries.values()) {
            startCmd.then(CommandManager.literal(panorama.panoramaName)
                    .then(CommandManager.argument("player", EntityArgumentType.players())
                            .executes(commandContext -> execute(commandContext, panorama, EntityArgumentType.getPlayers(commandContext, "player"))))
                    .executes(commandContext -> execute(commandContext, panorama, Collections.singleton(commandContext.getSource().getPlayer()))));
        }
        cmd.then(startCmd);

        cmd.then(CommandManager.literal("clear")
                .executes(context -> executeClear(context, Collections.singleton(context.getSource().getPlayer())))//clears just player
                .then(CommandManager.argument("player", EntityArgumentType.players())
                        .executes(context -> executeClear(context, EntityArgumentType.getPlayers(context, "player"))))//clears selector
                .then(CommandManager.literal("all").executes(context -> executeClear(context, null))));//clears all

        /*
        cmd.then(CommandManager.literal("testlen")
                .then(CommandManager.argument("begin", IntegerArgumentType.integer())
                        .then(CommandManager.argument("end", IntegerArgumentType.integer())
                                .executes(context -> {
                                    int begin = IntegerArgumentType.getInteger(context, "begin");
                                    int end = IntegerArgumentType.getInteger(context, "end");

                                    for (int i = begin; i < end; i++) {
                                        String output = "a" + Padder.smartPad(i, "") + "a";
                                        context.getSource().sendFeedback(Text.literal(output).setStyle(Style.EMPTY.withFont(new Identifier("4jmenu:panorama/1/day/3"))), false);
                                    }
                                    return 1;
                                }))
                        .executes(context -> {
                            int begin = IntegerArgumentType.getInteger(context, "begin");
                            String output = "a" + Padder.smartPad(begin, "") + "a";
                            context.getSource().sendFeedback(Text.literal(output).setStyle(Style.EMPTY.withFont(new Identifier("4jmenu:panorama/1/day/3"))), false);
                            return 1;
                        })));
         */
        dispatcher.register(cmd);
    }

    public static int executeClear(CommandContext<ServerCommandSource> commandContext, Collection<ServerPlayerEntity> players) {
        BossBarManager bossMan = commandContext.getSource().getServer().getBossBarManager();
        for (String panoramaName : PanoramaViewer.panoramaEntries.keySet()) {
            CommandBossBar bossBar = bossMan.get(new Identifier(PanoramaViewer.MOD_ID, panoramaName));
            if (bossBar != null) {
                if (players == null)
                    bossBar.clearPlayers();
                else
                    players.forEach(bossBar::removePlayer);

                if (bossBar.getPlayers().size() == 0)
                    bossMan.remove(bossBar);
            }
        }
        return 1;
    }

    public static int getScoreBoard(CommandContext<ServerCommandSource> commandContext, String panoramaName, Collection<ServerPlayerEntity> players) throws CommandSyntaxException {
        ScoreboardObjective obj = ScoreboardObjectiveArgumentType.getObjective(commandContext, "uiscaleOBJ");
        boolean isNight = BoolArgumentType.getBool(commandContext, "isNight");

        for (ServerPlayerEntity player : players) {
            int amount = commandContext.getSource().getServer().getScoreboard().getPlayerScore(player.getEntityName(), obj).getScore();
            Panorama panorama = PanoramaViewer.panoramaEntries.get(panoramaName + "_ui" + amount + (isNight ? "_night" : ""));
            execute(commandContext, panorama, Collections.singleton(player));
        }
        return 1;
    }

    public static int execute(CommandContext<ServerCommandSource> commandContext, Panorama panorama, Collection<ServerPlayerEntity> players) {
        if (panorama == null) {
            commandContext.getSource().sendFeedback(Text.literal("Panorama was not found"), false);
            return 1;
        }

        BossBarManager bossMan = commandContext.getSource().getServer().getBossBarManager();
        CommandBossBar bossBar = bossMan.get(new Identifier(PanoramaViewer.MOD_ID, panorama.panoramaName));
        if (bossBar == null) {
            bossBar = bossMan.add(new Identifier(PanoramaViewer.MOD_ID, panorama.panoramaName), panorama.getPaddedText());
        }

        for (ServerPlayerEntity player : players)
            bossBar.addPlayer(player);

        return 1;
    }
}