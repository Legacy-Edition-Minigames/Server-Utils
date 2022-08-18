package net.kyrptonaught.serverutils.takeEverything;


import blue.endless.jankson.Comment;
import net.kyrptonaught.kyrptconfig.config.AbstractConfigFile;

import java.util.HashSet;

public class TakeEverythingConfig implements AbstractConfigFile {
    @Comment("Enable/Disable Take Everything")
    public boolean Enabled = true;

    @Comment("Should currently worn item be deleted when being swapped")
    public boolean deleteItemNotDrop = false;

    @Comment("Enable take everything while in spectator")
    public boolean worksInSpectator = false;

    @Comment("List of player UUIDs that can swap items with enchants")
    public static HashSet<String> SWAP_IGNORE_ENCHANTS = new HashSet<>();

}
