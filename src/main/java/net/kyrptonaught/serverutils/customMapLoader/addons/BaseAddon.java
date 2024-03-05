package net.kyrptonaught.serverutils.customMapLoader.addons;

import net.kyrptonaught.serverutils.customMapLoader.MapSize;
import net.kyrptonaught.serverutils.datapackInteractables.BlockList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.nio.file.Path;

public class BaseAddon {

    public Identifier addon_id;

    private String name;
    private String name_key;

    private String description;
    private String description_key;

    public String authors;
    public String version;

    public String addon_type;
    public String addon_pack;
    public String addon_pack_key;

    public BlockList interactable_blocklist;

    public transient Path filePath;
    public transient boolean isBaseAddon = false;
    public transient boolean isAddonEnabled = true;

    public MutableText getNameText() {
        if (name_key != null)
            return Text.translatable(name_key);
        return Text.literal(name);
    }

    public MutableText getDescriptionText() {
        if (description_key != null)
            return Text.translatable(description_key);
        return Text.literal(description);
    }

    public MutableText getAddonPackText() {
        if (addon_pack_key != null)
            return Text.translatable(addon_pack_key);
        return Text.literal(addon_pack);
    }

    public String getDirectoryInZip(MapSize mapSize) {
        return "world/" + mapSize.fileName + "/";
    }
}
