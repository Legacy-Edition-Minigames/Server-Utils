package net.kyrptonaught.serverutils.customUI;

import net.kyrptonaught.serverutils.AbstractConfigFile;

import java.util.HashMap;

public class ScreenConfig extends AbstractConfigFile {
    public String title;

    public boolean escToClose = true;
    public String escSound;

    public int forceHotBarSlot = -1;

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

        public Boolean replaceOpenScreen;
        public String customModelData;

        public Boolean refreshOnInteract;

        public DynamicItem dynamicItem;

        public boolean replaceOpenScreen() {
            return replaceOpenScreen != null && replaceOpenScreen;
        }

        public boolean refreshOnInteract() {
            return refreshOnInteract != null && refreshOnInteract;
        }

        public boolean isFieldBlank(String field) {
            return field == null || field.isEmpty() || field.isBlank();
        }

        public boolean isDynamic() {
            return dynamicItem != null;
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

            if (replaceOpenScreen == null)
                replaceOpenScreen = other.replaceOpenScreen;

            if (isFieldBlank(customModelData))
                customModelData = other.customModelData;

            if (dynamicItem == null)
                dynamicItem = other.dynamicItem;

            if (refreshOnInteract == null)
                refreshOnInteract = other.refreshOnInteract;

            return this;
        }

        public static class DynamicItem {
            public String score;
            public String player;

            public HashMap<Integer, SlotDefinition> items;

        }

        public static DynamicItem EMPTY_ITEM = new DynamicItem();
    }
}
