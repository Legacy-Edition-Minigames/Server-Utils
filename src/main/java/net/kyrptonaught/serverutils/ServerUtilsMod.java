package net.kyrptonaught.serverutils;

import com.google.gson.Gson;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.serverutils.SpectateSqueaker.SpectateSqueakerMod;
import net.kyrptonaught.serverutils.advancementMenu.AdvancementMenuMod;
import net.kyrptonaught.serverutils.advancementSync.AdvancementSyncMod;
import net.kyrptonaught.serverutils.armorHudToggle.ArmorHudMod;
import net.kyrptonaught.serverutils.backendServer.BackendServerModule;
import net.kyrptonaught.serverutils.brandBlocker.BrandBlocker;
import net.kyrptonaught.serverutils.chatDisabler.ChatDisabler;
import net.kyrptonaught.serverutils.chestTracker.ChestTrackerMod;
import net.kyrptonaught.serverutils.cpslimiter.CPSLimiter;
import net.kyrptonaught.serverutils.critBlocker.CritBlockerMod;
import net.kyrptonaught.serverutils.customMapLoader.CustomMapLoaderMod;
import net.kyrptonaught.serverutils.customUI.CustomUI;
import net.kyrptonaught.serverutils.customWorldBorder.CustomWorldBorderMod;
import net.kyrptonaught.serverutils.datapackInteractables.DatapackInteractables;
import net.kyrptonaught.serverutils.dimensionLoader.DimensionLoaderMod;
import net.kyrptonaught.serverutils.discordBridge.DiscordBridgeMod;
import net.kyrptonaught.serverutils.dropevent.DropEventMod;
import net.kyrptonaught.serverutils.floodgateCompat.FloodgateCompatMod;
import net.kyrptonaught.serverutils.healthcmd.HealthCMDMod;
import net.kyrptonaught.serverutils.knockback.KnockbackMod;
import net.kyrptonaught.serverutils.noteblockMusic.NoteblockMusicMod;
import net.kyrptonaught.serverutils.panoramaViewer.PanoramaViewer;
import net.kyrptonaught.serverutils.personatus.PersonatusModule;
import net.kyrptonaught.serverutils.playerJoinLocation.PlayerJoinLocationMod;
import net.kyrptonaught.serverutils.playerlockdown.PlayerLockdownMod;
import net.kyrptonaught.serverutils.ride.RideMod;
import net.kyrptonaught.serverutils.scoreboardPlayerInfo.ScoreboardPlayerInfo;
import net.kyrptonaught.serverutils.scoreboardsuffix.ScoreboardSuffixMod;
import net.kyrptonaught.serverutils.serverTranslator.ServerTranslator;
import net.kyrptonaught.serverutils.smallInv.SmallInvMod;
import net.kyrptonaught.serverutils.snowballKnockback.SnowballKnockbackMod;
import net.kyrptonaught.serverutils.switchableresourcepacks.SwitchableResourcepacksMod;
import net.kyrptonaught.serverutils.syncedKeybinds.SyncedKeybinds;
import net.kyrptonaught.serverutils.takeEverything.TakeEverythingMod;
import net.kyrptonaught.serverutils.tntlighter.TNTLighter;
import net.kyrptonaught.serverutils.userConfig.UserConfigMod;
import net.kyrptonaught.serverutils.utilityCommands.UtilCommandsMod;
import net.kyrptonaught.serverutils.velocitymodifier.VelocityModifierModule;
import net.kyrptonaught.serverutils.velocityserverswitch.VelocityServerSwitchMod;
import net.kyrptonaught.serverutils.waterFreezer.WaterFreezer;
import net.kyrptonaught.serverutils.welcomeMessage.WelcomeModule;
import net.kyrptonaught.serverutils.whitelistSync.WhitelistSyncMod;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

import java.util.HashMap;

public class ServerUtilsMod implements ModInitializer {
    public static String MOD_ID = "serverutils";
    public static Identifier PRESENCE_PACKET = new Identifier(MOD_ID, "presence");

    public static ConfigManager config = new ConfigManager(MOD_ID);
    public static HashMap<String, Module> modules = new HashMap<>();

    public static final BackendServerModule backendModule = new BackendServerModule("backend");

    public static WaterFreezer WaterFreezerModule = (WaterFreezer) registerModule("waterfreezer", new WaterFreezer());
    public static Module VelocityServerSwitchMod = registerModule("velocityserverswitch", new VelocityServerSwitchMod());
    public static Module VelocityModifierModule = registerModule("velocitycommand", new VelocityModifierModule());
    public static Module TNTLighterModule = registerModule("tntlighter", new TNTLighter());
    public static TakeEverythingMod TakeEverythingModule = (TakeEverythingMod) registerModule("takeeverything", new TakeEverythingMod());
    public static SyncedKeybinds SyncedKeybindsModule = (SyncedKeybinds) registerModule("syncedkeybinds", new SyncedKeybinds());
    public static SwitchableResourcepacksMod SwitchableResourcepacksModule = (SwitchableResourcepacksMod) registerModule("switchableresourcepacks", new SwitchableResourcepacksMod());
    public static Module SpectatorSqueakModule = registerModule("spectatesqueak", new SpectateSqueakerMod());
    public static Module ScoreboardSuffixModule = registerModule("scoreboardsuffix", new ScoreboardSuffixMod());
    public static Module ScoreboardPlayerInfoModule = registerModule("scoreboardplayerinfo", new ScoreboardPlayerInfo());
    public static Module RideModule = registerModule("ride", new RideMod());
    public static Module PlayerLockdownModule = registerModule("playerlockdown", new PlayerLockdownMod());
    public static PanoramaViewer PanoramaViewerModule = (PanoramaViewer) registerModule("panoramaviewer", new PanoramaViewer());
    public static Module HealthCommandModule = registerModule("healthcmd", new HealthCMDMod());
    public static DropEventMod DropEventModule = (DropEventMod) registerModule("dropevent", new DropEventMod());
    public static Module DimensionLoaderModule = registerModule("dimensionloader", new DimensionLoaderMod());
    public static Module DatapackInteractablesModule = registerModule("interactables", new DatapackInteractables());
    public static Module CPSLimiterModule = registerModule("cpslimiter", new CPSLimiter());
    public static Module ChestTrackerModule = registerModule("chesttracker", new ChestTrackerMod());
    public static ChatDisabler ChatDisabler = (ChatDisabler) registerModule("chatdisabler", new ChatDisabler());
    public static BrandBlocker BrandBlockerModule = (BrandBlocker) registerModule("brandblocker", new BrandBlocker());
    public static AdvancementSyncMod AdvancementSyncModule = (AdvancementSyncMod) registerModule("advancementsync", new AdvancementSyncMod());
    public static WelcomeModule WelcomeMessageModule = (WelcomeModule) registerModule("welcomemessage", new WelcomeModule());
    public static PersonatusModule personatusModule = (PersonatusModule) registerModule("personatus", new PersonatusModule());
    public static Module critBlockerModule = registerModule("critblocker", new CritBlockerMod());
    public static Module CustomUIModule = registerModule("customui", new CustomUI());
    public static ServerTranslator ServerTranslatorModule = (ServerTranslator) registerModule("servertranslator", new ServerTranslator());
    public static Module CustomWorldBorder = registerModule("customworldborder", new CustomWorldBorderMod());
    public static DiscordBridgeMod DiscordBridgeModule = (DiscordBridgeMod) registerModule("discordbridge", new DiscordBridgeMod());
    public static UserConfigMod UserConfigModule = (UserConfigMod) registerModule("userconfig", new UserConfigMod());
    public static WhitelistSyncMod whitelistSyncMod = (WhitelistSyncMod) registerModule("whitelistsync", new WhitelistSyncMod());
    public static SnowballKnockbackMod snowballKnockback = (SnowballKnockbackMod) registerModule("snowballknockback", new SnowballKnockbackMod());
    public static Module armorHudModule = registerModule("armorhud", new ArmorHudMod());
    public static Module noteblockMusic = registerModule("noteblockmusic", new NoteblockMusicMod());
    public static Module floodgateCompatModule = registerModule("floodgatecompat", new FloodgateCompatMod());
    public static PlayerJoinLocationMod playerJoinLocationMod = (PlayerJoinLocationMod) registerModule("playerjoinlocation", new PlayerJoinLocationMod());
    public static Module knockbackModule = registerModule("knockback", new KnockbackMod());
    public static Module customMapLoaderModule = registerModule("custommaploader", new CustomMapLoaderMod());
    public static Module utilModule = registerModule("util", new UtilCommandsMod());
    public static Module smallInvModule = registerModule("smallinv", new SmallInvMod());
    public static Module advancementMenu = registerModule("advancementmenu", new AdvancementMenuMod());

    @Override
    public void onInitialize() {
        registerPresence();

        backendModule.setConfig(config.load(backendModule.getMOD_ID(), backendModule.getDefaultConfig()));
        backendModule.saveConfig();
        backendModule.onInitialize();

        for (Module module : modules.values()) {
            module.onInitialize();
            if (module instanceof ModuleWConfig<?> moduleWConfig) {
                moduleWConfig.setConfig(config.load(module.getMOD_ID(), moduleWConfig.getDefaultConfig()));
                moduleWConfig.saveConfig();
            }
        }
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerCommand(dispatcher);
            for (Module module : modules.values())
                module.registerCommands(dispatcher, registryAccess);
        });

    }

    public static Module registerModule(String MOD_ID, Module module) {
        module.setMOD_ID(MOD_ID);
        modules.put(MOD_ID, module);
        return module;
    }

    public void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("serverutils")
                .requires((source) -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("reloadAllConfigs")
                        .executes(context -> {
                            for (Module module : modules.values()) {
                                if (module instanceof ModuleWConfig<?> moduleWConfig) {
                                    moduleWConfig.setConfig(config.load(module.getMOD_ID(), moduleWConfig.getDefaultConfig()));
                                    moduleWConfig.saveConfig();
                                }
                            }
                            context.getSource().sendFeedback(CMDHelper.getFeedbackLiteral("Configs reloaded. Note: not all modules may reflect these changes"), false);
                            return 1;
                        })));
    }

    public static void registerPresence() {
        //used by the client to detect if connected on a server with this mod installed
        ServerPlayNetworking.registerGlobalReceiver(PRESENCE_PACKET, (server, player, handler, buf, responseSender) -> {
        });
    }

    public static Gson getGson() {
        return config.getGSON();
    }
}
