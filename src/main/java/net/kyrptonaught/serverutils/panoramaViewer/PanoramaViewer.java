package net.kyrptonaught.serverutils.panoramaViewer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.entity.boss.BossBarManager;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.util.Identifier;

import java.util.HashMap;

public class PanoramaViewer {
    public static final String MOD_ID = "panoramaviewer";

    public static HashMap<String, Panorama> panoramaEntries = new HashMap<>();

    public static HashMap<String, FrameCounter> frameCounters = new HashMap<>();
    public static HashMap<String, Padder> padders = new HashMap<>();

    public static void onInitialize() {
        ServerUtilsMod.configManager.registerFile(MOD_ID, new PanoramaConfig());
        ServerUtilsMod.configManager.load(MOD_ID);

        for (String ui : getConfig().FrameCounts.keySet()) {
            frameCounters.put(ui, new FrameCounter(getConfig().FrameCounts.get(ui)));
            padders.put(ui, new Padder(getConfig().PaddingSize.get(ui)));
        }

        createPanoramasFromConfig();

        CommandRegistrationCallback.EVENT.register(PanoramaCommand::registerCommand);

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
            frameCounters.values().forEach(FrameCounter::readyForFirstTick);

            BossBarManager bossMan = server.getBossBarManager();
            for (Panorama panorama : panoramaEntries.values()) {
                CommandBossBar bossBar = bossMan.get(new Identifier(MOD_ID, panorama.panoramaName));
                if (!panorama.frameCounter.doesTick() || bossBar == null || bossBar.getPlayers().size() == 0) continue;

                panorama.tickFrameCounter();
                bossBar.setName(panorama.getPaddedText());
            }
        });
    }

    public static void createPanoramasFromConfig() {
        getConfig().autoEntries.forEach(panoramaEntry -> panoramaEntry.texts.forEach((ui, text) -> {
            FrameCounter frameCounter = frameCounters.get(ui);
            Padder padder = padders.get(ui);

            String name = panoramaEntry.panoramaName + "_ui" + ui;
            checkConfigEntryForErrors(name, text, frameCounter, padder);
            panoramaEntries.put(name, new Panorama(name, text, frameCounter, padder));

            if (panoramaEntry.hasNightVariant) {
                name += "_night";
                panoramaEntries.put(name, new Panorama(name, text.replace("day", "night"), frameCounter, padder));
            }
        }));
        getConfig().manualEntries.forEach(manualPanoramaEntry -> {
            FrameCounter frameCounter = frameCounters.get(manualPanoramaEntry.frameCounter);
            Padder padder = padders.get(manualPanoramaEntry.padder);
            checkConfigEntryForErrors(manualPanoramaEntry.panoramaName, manualPanoramaEntry.text, frameCounter, padder);


            panoramaEntries.put(manualPanoramaEntry.panoramaName, new Panorama(manualPanoramaEntry.panoramaName, manualPanoramaEntry.text, frameCounter, padder));
        });
    }

    private static void checkConfigEntryForErrors(String name, String text, FrameCounter frameCounter, Padder padder) {
        if (name == null || name.isBlank())
            System.out.println("Panorama without name found");
        if (text == null || text.isBlank())
            System.out.println("Panorama: " + name + " has invalid text");
        if (frameCounter == null)
            System.out.println("Panorama: " + name + " has invalid frameCounter");
        if (padder == null)
            System.out.println("Panorama: " + name + " has invalid padder");
    }

    public static PanoramaConfig getConfig() {
        return ((PanoramaConfig) ServerUtilsMod.configManager.getConfig(MOD_ID));
    }
}