package net.kyrptonaught.serverutils.dropevent;

import net.kyrptonaught.serverutils.ServerUtilsMod;

public class DropEventMod {

    public static final String MOD_ID = "dropevent";

    public static void onInitialize() {
        ServerUtilsMod.configManager.registerFile(MOD_ID, new DropEventConfig());
    }

    public static DropEventConfig getConfig() {
        return (DropEventConfig) ServerUtilsMod.configManager.getConfig(MOD_ID);
    }
}
