package net.kyrptonaught.serverutils.personatus;

import net.kyrptonaught.serverutils.ModuleWConfig;
import net.kyrptonaught.serverutils.ServerUtilsMod;

public class PersonatusModule extends ModuleWConfig<PersonatusConfig> {

    public void onInitialize() {

    }

    public static boolean isEnabled() {
        return ServerUtilsMod.personatusModule.getConfig().enabled;
    }

    @Override
    public PersonatusConfig createDefaultConfig() {
        return new PersonatusConfig();
    }
}
