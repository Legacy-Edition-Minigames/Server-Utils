package net.kyrptonaught.serverutils;

import net.fabricmc.api.ModInitializer;
import net.kyrptonaught.kyrptconfig.config.ConfigManager;
import net.kyrptonaught.serverutils.dropevent.DropEventMod;
import net.kyrptonaught.serverutils.playerlockdown.PlayerLockdownMod;
import net.kyrptonaught.serverutils.scoreboardsuffix.ScoreboardSuffixMod;
import net.kyrptonaught.serverutils.switchableresourcepacks.SwitchableResourcepacksMod;
import net.kyrptonaught.serverutils.takeEverything.TakeEverythingMod;
import net.kyrptonaught.serverutils.velocitymodifier.VelocityCommandMod;
import net.kyrptonaught.serverutils.velocityserverswitch.VelocityServerSwitchMod;

public class ServerUtilsMod implements ModInitializer {
    public static String MOD_ID = "serverutils";
    public static ConfigManager.MultiConfigManager configManager = new ConfigManager.MultiConfigManager(MOD_ID);

    @Override
    public void onInitialize() {
        PlayerLockdownMod.onInitialize();
        ScoreboardSuffixMod.onInitialize();
        SwitchableResourcepacksMod.onInitialize();
        TakeEverythingMod.onInitialize();
        VelocityCommandMod.onInitialize();
        VelocityServerSwitchMod.onInitialize();
        DropEventMod.onInitialize();

        configManager.load();
    }
}
