package net.kyrptonaught.serverutils.floodgateCompat;

import net.fabricmc.loader.api.FabricLoader;
import net.kyrptonaught.serverutils.Module;

public class FloodgateCompatMod extends Module {

    @Override
    public void onInitialize() {
        //temp hack to fix compat with Fabric Proxy lite/floodgate/LuckPerms
        if (FabricLoader.getInstance().isModLoaded("fabricproxy-lite"))
            FabricProxyLite.hackEarlySendFix();
    }
}
