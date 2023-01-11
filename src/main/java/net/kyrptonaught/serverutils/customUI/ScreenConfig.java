package net.kyrptonaught.serverutils.customUI;

import net.kyrptonaught.serverutils.AbstractConfigFile;

import java.util.HashMap;

public class ScreenConfig extends AbstractConfigFile {
    public String title;

    public boolean escToClose = true;

    public HashMap<String, SlotDefinition> presets = new HashMap<>();

    public HashMap<Integer, SlotDefinition> slots = new HashMap<>();


    public static class SlotDefinition {
        public String itemID;
        public String itemNBT;
        public String displayName;
        public String leftClickAction;
        public String rightClickAction;

        public String presetID;

    }
}
