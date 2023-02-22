package net.kyrptonaught.serverutils.customUI;

import net.kyrptonaught.serverutils.AbstractConfigFile;

import java.util.HashMap;

public class ScreenConfig extends AbstractConfigFile {
    public String title;

    public boolean escToClose = true;
    public boolean replaceOpenScreen = false;

    public HashMap<String, SlotDefinition> presets = new HashMap<>();

    public HashMap<String, SlotDefinition> slots = new HashMap<>();


    public static class SlotDefinition {
        public String itemID;
        public String itemNBT;
        public String displayName;
        public String leftClickAction;
        public String rightClickAction;
        public String leftClickSound;
        public String rightClickSound;
        public String presetID;

        public String customModelData;

        public boolean isFieldBlank(String field) {
            return field == null || field.isEmpty() || field.isBlank();
        }

        public SlotDefinition copyFrom(SlotDefinition other) {
            if (isFieldBlank(itemID))
                itemID = other.itemID;

            if (isFieldBlank(itemNBT))
                itemNBT = other.itemNBT;

            if (isFieldBlank(displayName))
                displayName = other.displayName;

            if (isFieldBlank(leftClickAction))
                leftClickAction = other.leftClickAction;

            if (isFieldBlank(rightClickAction))
                rightClickAction = other.rightClickAction;

            if (isFieldBlank(leftClickSound))
                leftClickSound = other.leftClickSound;

            if (isFieldBlank(rightClickSound))
                rightClickSound = other.rightClickSound;

            if (isFieldBlank(presetID))
                presetID = other.presetID;

            if (isFieldBlank(customModelData))
                customModelData = other.customModelData;

            return this;
        }
    }
}
