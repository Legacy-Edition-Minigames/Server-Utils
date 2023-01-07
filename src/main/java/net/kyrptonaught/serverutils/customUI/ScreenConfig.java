package net.kyrptonaught.serverutils.customUI;

import net.kyrptonaught.serverutils.AbstractConfigFile;

import java.util.HashMap;

public class ScreenConfig extends AbstractConfigFile {
    String title;

    public HashMap<Integer, SlotDefinition> slots;


    public static class SlotDefinition {
        public String itemID;
        public String itemNBT;
        public String displayName;
        public String leftClickAction;
        public String rightClickAction;

    }
}
