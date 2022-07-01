package net.kyrptonaught.serverutils.panoramaViewer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class PanoramaViewer {
    public static final String MOD_ID = "panoramaviewer";

    public static HashMap<String, Panorama> panoramaEntries = new HashMap<>();

    public static HashMap<Integer, FrameCounter> frameCounters = new HashMap<>();
    public static HashMap<Integer, Padder> padders = new HashMap<>();

    public static void onInitialize() {
        ServerUtilsMod.configManager.registerFile(MOD_ID, new PanoramaConfig());
        ServerUtilsMod.configManager.load(MOD_ID);

        for (Integer ui : getConfig().FrameCounts.keySet()) {
            frameCounters.put(ui, new FrameCounter(getConfig().FrameCounts.get(ui), true));
            padders.put(ui, new Padder(frameCounters.get(ui), getConfig().PaddingSize.get(ui)));
        }

        createPanoramasFromConfig();

        CommandRegistrationCallback.EVENT.register(PanoramaViewer::register);

        if (getConfig().autoEntries.size() == 0) {
            PanoramaConfig.AutoPanoramaEntry option = new PanoramaConfig.AutoPanoramaEntry();
            option.panoramaName = "example_panorama";
            option.texts.put("1", "{\"text\":\"This is an example panorama\",\"font\":\"minecraft:default\"}");
            option.texts.put("2", "{\"text\":\"This is an example panorama\",\"font\":\"minecraft:default\"}");
            option.hasNightVariant = true;
            getConfig().autoEntries.add(option);
            ServerUtilsMod.configManager.save(MOD_ID);
            System.out.println("[" + MOD_ID + "]: Generated example config");
        }

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            frameCounters.values().forEach(frameCounter -> frameCounter.tick(true));
            padders.values().forEach(padder -> padder.updatePadding());

            BossBarManager bossMan = server.getBossBarManager();
            for (Panorama panorama : panoramaEntries.values()) {
                CommandBossBar bossBar = bossMan.get(new Identifier(MOD_ID, panorama.panoramaName));
                if (!panorama.frameCounter.doesTick() || bossBar == null || bossBar.getPlayers().size() == 0) continue;

                panorama.tickFrameCounter();
                bossBar.setName(panorama.padder.padOutput(panorama.text));
            }
        });
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean b) {
        LiteralArgumentBuilder<ServerCommandSource> cmd = CommandManager.literal("panorama")
                .requires((source) -> source.hasPermissionLevel(2));

        LiteralArgumentBuilder<ServerCommandSource> startCmd = CommandManager.literal("start");
        for (PanoramaConfig.AutoPanoramaEntry panoramaEntry : getConfig().autoEntries) {
            startCmd.then(CommandManager.literal(panoramaEntry.panoramaName)
                    .then(CommandManager.argument("uiscaleOBJ", ScoreboardObjectiveArgumentType.scoreboardObjective())
                            .then(CommandManager.argument("isNight", BoolArgumentType.bool())
                                    .then(CommandManager.argument("player", EntityArgumentType.players())
                                            .executes(commandContext -> getScoreBoard(commandContext, panoramaEntry.panoramaName, EntityArgumentType.getPlayers(commandContext, "player"))))
                                    .executes(commandContext -> getScoreBoard(commandContext, panoramaEntry.panoramaName, Collections.singleton(commandContext.getSource().getPlayer()))))));

        }

        for (Panorama panorama : panoramaEntries.values()) {
            startCmd.then(CommandManager.literal(panorama.panoramaName)
                    .then(CommandManager.argument("player", EntityArgumentType.players())
                            .executes(commandContext -> execute(commandContext, panorama, EntityArgumentType.getPlayers(commandContext, "player"))))
                    .executes(commandContext -> execute(commandContext, panorama, Collections.singleton(commandContext.getSource().getPlayer()))));
        }
        cmd.then(startCmd);

        cmd.then(CommandManager.literal("clear").executes(context -> {
            BossBarManager bossMan = context.getSource().getServer().getBossBarManager();
            for (String panoramaName : panoramaEntries.keySet()) {
                CommandBossBar bossBar = bossMan.get(new Identifier(MOD_ID, panoramaName));
                if (bossBar != null) {
                    bossBar.clearPlayers();
                    bossMan.remove(bossBar);
                }
            }
            return 1;
        }));
        dispatcher.register(cmd);
    }

    public static int getScoreBoard(CommandContext<ServerCommandSource> commandContext, String panoramaName, Collection<ServerPlayerEntity> players) throws CommandSyntaxException {
        ScoreboardObjective obj = ScoreboardObjectiveArgumentType.getObjective(commandContext, "uiscaleOBJ");
        boolean isNight = BoolArgumentType.getBool(commandContext, "isNight");

        for (ServerPlayerEntity player : players) {
            int amount = commandContext.getSource().getServer().getScoreboard().getPlayerScore(player.getEntityName(), obj).getScore();
            Panorama panorama = panoramaEntries.get(panoramaName + "_ui" + amount + (isNight ? "_night" : ""));
            execute(commandContext, panorama, Collections.singleton(player));
        }
        return 1;
    }

    public static int execute(CommandContext<ServerCommandSource> commandContext, Panorama panorama, Collection<ServerPlayerEntity> players) {
        if (panorama == null) {
            commandContext.getSource().sendFeedback(new LiteralText("Panorama was not found"), false);
            return 1;
        }

        BossBarManager bossMan = commandContext.getSource().getServer().getBossBarManager();
        CommandBossBar bossBar = bossMan.get(new Identifier(MOD_ID, panorama.panoramaName));
        if (bossBar == null) {
            bossBar = bossMan.add(new Identifier(MOD_ID, panorama.panoramaName), panorama.padder.padOutput(panorama.text));
        }
        bossBar.addPlayers(players);
        return 1;
    }

    public static void createPanoramasFromConfig() {
        getConfig().autoEntries.forEach(panoramaEntry -> panoramaEntry.texts.forEach((strUI, text) -> {
            int ui = Integer.parseInt(strUI);
            FrameCounter frameCounter = frameCounters.get(ui);
            Padder padder = padders.get(ui);

            String name = panoramaEntry.panoramaName + "_ui" + ui;
            panoramaEntries.put(name, new Panorama(name, text, frameCounter, padder));

            if (panoramaEntry.hasNightVariant) {
                name += "_night";
                panoramaEntries.put(name, new Panorama(name, text.replace("day", "night"), frameCounter, padder));
            }
        }));
    }

    public static PanoramaConfig getConfig() {
        return ((PanoramaConfig) ServerUtilsMod.configManager.getConfig(MOD_ID));
    }
}