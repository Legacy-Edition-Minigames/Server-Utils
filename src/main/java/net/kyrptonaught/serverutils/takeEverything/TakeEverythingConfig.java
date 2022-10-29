package net.kyrptonaught.serverutils.takeEverything;


import net.kyrptonaught.serverutils.AbstractConfigFile;

import java.util.HashSet;

public class TakeEverythingConfig extends AbstractConfigFile {

    public boolean Enabled = true;

    public boolean deleteItemNotDrop = false;

    public boolean worksInSpectator = false;

    public static HashSet<String> SWAP_IGNORE_ENCHANTS = new HashSet<>();
}
