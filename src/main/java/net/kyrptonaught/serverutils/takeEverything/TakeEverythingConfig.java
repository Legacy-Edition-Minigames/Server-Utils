package net.kyrptonaught.serverutils.takeEverything;


import net.kyrptonaught.kyrptconfig.config.AbstractConfigFile;

import java.util.HashSet;

public class TakeEverythingConfig implements AbstractConfigFile {
    public boolean Enabled = true;

    public boolean deleteItemNotDrop = false;

    public boolean worksInSpectator = false;

    public static transient final HashSet<String> SWAP_IGNORE_ENCHANTS = new HashSet<>();

}
