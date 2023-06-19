package net.kyrptonaught.serverutils.userConfig;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.advancementSync.AdvancementSyncMod;
import net.kyrptonaught.serverutils.backendServer.BackendServerModule;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.*;

public class UserConfigStorage {
    private static final HashMap<UUID, HashMap<Identifier, String>> userConfigs = new HashMap<>();
    private static final HashMap<Identifier, Set<Identifier>> groups = new HashMap<>();

    public static void setValue(ServerPlayerEntity player, Identifier key, String value) {
        setValueInternal(player, key, value);
        syncValue(player, key, value);
    }

    private static void setValueInternal(ServerPlayerEntity player, Identifier key, String value) {
        if (!userConfigs.containsKey(player.getUuid())) userConfigs.put(player.getUuid(), new HashMap<>());

        userConfigs.get(player.getUuid()).put(key, value);
    }

    public static String getValue(ServerPlayerEntity player, Identifier key) {
        if (!userConfigs.containsKey(player.getUuid())) userConfigs.put(player.getUuid(), new HashMap<>());

        return userConfigs.get(player.getUuid()).get(key);
    }

    public static void removeValue(ServerPlayerEntity player, Identifier key) {
        if (userConfigs.containsKey(player.getUuid())) {
            userConfigs.get(player.getUuid()).remove(key);

            if (userConfigs.get(player.getUuid()).size() == 0)
                userConfigs.remove(player.getUuid());
        }
        syncRemove(player, key);
    }

    public static void removeGroup(Identifier groupID) {
        groups.remove(groupID);
    }

    public static void addToGroup(Identifier groupID, Identifier key) {
        if (!groups.containsKey(groupID)) groups.put(groupID, new HashSet<>());

        groups.get(groupID).add(key);
    }

    public static void saveGroupToPreset(ServerPlayerEntity player, Identifier groupID, Identifier presetID) {
        JsonObject array = new JsonObject();
        for(Identifier config :groups.get(groupID)){
            array.addProperty(config.toString(), getValue(player, config));
        }

        BackendServerModule.asyncPost(AdvancementSyncMod.getUrl("userConfigSaveToPreset", player) + "/" + presetID, array.toString(), (success, response) -> {
            if (!success)
                System.out.println("Syncing user config for " + player.getDisplayName().getString() + " failed");
        });
    }

    public static void loadGroupFromPreset(ServerPlayerEntity player, Identifier groupID, Identifier presetID) {
        JsonArray array = new JsonArray();
        groups.get(groupID).forEach(id -> array.add(id.toString()));
        BackendServerModule.asyncPost(AdvancementSyncMod.getUrl("userConfigLoadFromPreset", player) + "/" + presetID, array.toString(), (success, response) -> {
            if (success) loadPlayer(player);
            else {
                System.out.println("Syncing user config for " + player.getDisplayName().getString() + " failed");
            }
        });
    }

    public static void unloadPlayer(ServerPlayerEntity player) {
        userConfigs.remove(player.getUuid());
    }

    public static void loadPlayer(ServerPlayerEntity player) {
        try {
            BackendServerModule.asyncGet(AdvancementSyncMod.getUrl("getUserConfig", player), (success, response) -> {
                if (success) {
                    JsonObject object = ServerUtilsMod.getGson().fromJson(response.body(), JsonObject.class);
                    for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                        setValueInternal(player, new Identifier(entry.getKey()), entry.getValue().getAsString());
                    }
                } else {
                    System.out.println("Syncing user config for " + player.getDisplayName().getString() + " failed");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void syncValue(ServerPlayerEntity player, Identifier key, String value) {
        try {
            BackendServerModule.asyncPost(AdvancementSyncMod.getUrl("syncUserConfig", player) + "/" + key + "/" + value.replaceAll(" ", "%20"), (success, response) -> {
                if (!success)
                    System.out.println("Syncing user config for " + player.getDisplayName().getString() + " failed");
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void syncRemove(ServerPlayerEntity player, Identifier key) {
        try {
            BackendServerModule.asyncPost(AdvancementSyncMod.getUrl("removeUserConfig", player) + "/" + key, (success, response) -> {
                if (!success)
                    System.out.println("Syncing user config for " + player.getDisplayName().getString() + " failed");
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
