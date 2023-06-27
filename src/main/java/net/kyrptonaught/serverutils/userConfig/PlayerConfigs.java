package net.kyrptonaught.serverutils.userConfig;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PlayerConfigs {

    public final HashMap<Identifier, String> configs = new HashMap<>();
    public final HashMap<Identifier, HashMap<Identifier, String>> presets = new HashMap<>();

    public void setValue(Identifier key, String value) {
        configs.put(key, value);
    }

    public String getValue(Identifier key) {
        return configs.get(key);
    }

    public void removeValue(Identifier key) {
        configs.remove(key);
    }

    public void saveToPreset(Identifier presetID, Set<Identifier> group) {
        for (Identifier config : group) {
            setPresetValueInternal(presetID, config, configs.get(config));
        }
    }

    private void setPresetValueInternal(Identifier presetID, Identifier key, String value) {
        if (!presets.containsKey(presetID)) presets.put(presetID, new HashMap<>());

        presets.get(presetID).put(key, value);
    }

    public void loadFromPreset(Identifier presetID, Set<Identifier> group) {
        if (!presets.containsKey(presetID)) return;

        for (Identifier config : group) {
            configs.put(config, presets.get(presetID).get(config));
        }
    }

    public static PlayerConfigs load(JsonObject jsonObject) {
        PlayerConfigs playerConfigs = new PlayerConfigs();
        if (jsonObject == null) return playerConfigs;

        JsonObject configs = jsonObject.getAsJsonObject("configs");
        if (configs != null)
            for (Map.Entry<String, JsonElement> innerEntry : configs.entrySet())
                playerConfigs.setValue(new Identifier(innerEntry.getKey()), innerEntry.getValue().getAsString());

        JsonObject presets = jsonObject.getAsJsonObject("presets");
        if (presets != null)
            for (Map.Entry<String, JsonElement> innerEntry : presets.entrySet())
                for (Map.Entry<String, JsonElement> innerEntry2 : innerEntry.getValue().getAsJsonObject().entrySet())
                    playerConfigs.setPresetValueInternal(new Identifier(innerEntry.getKey()), new Identifier(innerEntry2.getKey()), innerEntry2.getValue().getAsString());

        return playerConfigs;
    }
}
