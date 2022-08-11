package net.kyrptonaught.serverutils;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.kyrptconfig.config.ConfigManager;
import net.kyrptonaught.serverutils.brandBlocker.BrandBlocker;
import net.kyrptonaught.serverutils.chestTracker.ChestTrackerMod;
import net.kyrptonaught.serverutils.dropevent.DropEventMod;
import net.kyrptonaught.serverutils.healthcmd.HealthCMDMod;
import net.kyrptonaught.serverutils.panoramaViewer.PanoramaViewer;
import net.kyrptonaught.serverutils.playerlockdown.PlayerLockdownMod;
import net.kyrptonaught.serverutils.scoreboardPlayerInfo.ScoreboardPlayerInfo;
import net.kyrptonaught.serverutils.ride.RideMod;
import net.kyrptonaught.serverutils.scoreboardsuffix.ScoreboardSuffixMod;
import net.kyrptonaught.serverutils.switchableresourcepacks.SwitchableResourcepacksMod;
import net.kyrptonaught.serverutils.takeEverything.TakeEverythingMod;
import net.kyrptonaught.serverutils.tntlighter.TNTLighter;
import net.kyrptonaught.serverutils.velocitymodifier.VelocityCommandMod;
import net.kyrptonaught.serverutils.velocityserverswitch.VelocityServerSwitchMod;
import net.kyrptonaught.serverutils.waterFreezer.WaterFreezer;
import net.minecraft.util.Identifier;

public class ServerUtilsMod implements ModInitializer {
    public static String MOD_ID = "serverutils";
    public static ConfigManager.MultiConfigManager configManager = new ConfigManager.MultiConfigManager(MOD_ID);
    public static Identifier PRESENCE_PACKET = new Identifier(MOD_ID, "presence");

    @Override
    public void onInitialize() {
        PlayerLockdownMod.onInitialize();
        ScoreboardSuffixMod.onInitialize();
        SwitchableResourcepacksMod.onInitialize();
        TakeEverythingMod.onInitialize();
        VelocityCommandMod.onInitialize();
        VelocityServerSwitchMod.onInitialize();
        DropEventMod.onInitialize();
        HealthCMDMod.onInitialize();
        ChestTrackerMod.onInitialize();
        WaterFreezer.onInitialize();
        RideMod.onInitialize();
        PanoramaViewer.onInitialize();
        TNTLighter.onInitialize();
        BrandBlocker.onInitialize();
        ScoreboardPlayerInfo.onInitialize();

        configManager.load();
        registerPresence();
    }

    public static void registerPresence() {
        //used by the client to detect if connected on a server with this mod installed
        ServerPlayNetworking.registerGlobalReceiver(PRESENCE_PACKET, (server, player, handler, buf, responseSender) -> {
        });
    }
}
