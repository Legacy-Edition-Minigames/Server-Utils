package net.kyrptonaught.serverutils.brandBlocker;

import net.kyrptonaught.serverutils.ServerUtilsMod;

public class BrandBlocker {
    public static String MOD_ID = "brandblocker";

    public static void onInitialize() {
        ServerUtilsMod.configManager.registerFile(MOD_ID, new BrandBlockerConfig());
    }

    public static BrandBlockerConfig getConfig() {
        return (BrandBlockerConfig) ServerUtilsMod.configManager.getConfig(MOD_ID);
    }
}