package net.kyrptonaught.serverutils.userConfig;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.backendLink.BackendServer;
import net.kyrptonaught.serverutils.backendLink.personatus.PersonatusProfile;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UserConfigStorage {
    private static final HashMap<UUID, PlayerConfigs> playerCache = new HashMap<>();
    private static final HashMap<Identifier, Set<Identifier>> groups = new HashMap<>();

    public static void setValue(ServerPlayerEntity player, Identifier key, String value) {
        setValue(player.getUuid(), key, value);
    }

    public static void setValue(UUID player, Identifier key, String value) {
        playerCache.get(player).setValue(key, value);
    }

    public static String getValue(ServerPlayerEntity player, Identifier key) {
        return playerCache.get(player.getUuid()).getValue(key);
    }

    public static void removeValue(ServerPlayerEntity player, Identifier key) {
        playerCache.get(player.getUuid()).removeValue(key);
    }

    public static void unlockAdvancement(ServerPlayerEntity player, Identifier id, String criteria) {
        if (id.getPath().contains("resource/") || id.getPath().contains("game/combat/")) return;
        playerCache.get(player.getUuid()).addAdvancement(id + "__" + criteria);
        syncPlayer(player);
    }

    public static void revokeAdvancement(ServerPlayerEntity player, Identifier id, String criteria) {
        playerCache.get(player.getUuid()).removeAdvancement(id + "__" + criteria);
        syncPlayer(player);
    }

    public static boolean hasAdvancement(ServerPlayerEntity player, Identifier id, String criteria) {
        return playerCache.get(player.getUuid()).hasAdvancement(id + "__" + criteria);
    }

    public static void removeGroup(Identifier groupID) {
        groups.remove(groupID);
    }

    public static void addToGroup(Identifier groupID, Identifier key) {
        if (!groups.containsKey(groupID)) groups.put(groupID, new HashSet<>());

        groups.get(groupID).add(key);
    }

    public static void saveGroupToPreset(ServerPlayerEntity player, Identifier groupID, Identifier presetID) {
        playerCache.get(player.getUuid()).saveToPreset(presetID, groups.get(groupID));
    }

    public static void loadGroupFromPreset(ServerPlayerEntity player, Identifier groupID, Identifier presetID) {
        playerCache.get(player.getUuid()).loadFromPreset(presetID, groups.get(groupID));
    }

    public static void unloadPlayer(ServerPlayerEntity player) {
        playerCache.remove(player.getUuid());
    }

    public static void unloadPlayer(GameProfile profile) {
        playerCache.remove(profile.getId());
    }

    public static void loadPlayer(ServerPlayerEntity player) {
        try {
            playerCache.put(player.getUuid(), new PlayerConfigs());

            String result = BackendServer.get(getUrl("getUserConfig", player));
            if (result != null) {
                playerCache.put(player.getUuid(), PlayerConfigs.load(ServerUtilsMod.getGson().fromJson(result, JsonObject.class)));
            } else {
                System.out.println("Loading user config for " + player.getDisplayName().getString() + " failed... loading local");
                playerCache.put(player.getUuid(), PlayerConfigs.load(UserConfigLocalStorage.loadPlayer(player.getUuidAsString())));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadPlayer(GameProfile profile) {
        try {
            playerCache.put(profile.getId(), new PlayerConfigs());

            String result = BackendServer.get(getUrl("getUserConfig", profile));
            if (result != null) {
                playerCache.put(profile.getId(), PlayerConfigs.load(ServerUtilsMod.getGson().fromJson(result, JsonObject.class)));
            } else {
                System.out.println("Loading user config for " + profile.getName() + " failed... loading local");
                playerCache.put(profile.getId(), PlayerConfigs.load(UserConfigLocalStorage.loadPlayer(profile.getId().toString())));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void syncPlayer(ServerPlayerEntity player) {
        String json = ServerUtilsMod.getGson().toJson(playerCache.get(player.getUuid()));
        BackendServer.asyncPost(getUrl("syncUserConfig", player), json, (success, response) -> {
            if (!success) {
                System.out.println("Syncing user config for " + player.getDisplayName().getString() + " failed... saving local");
                UserConfigLocalStorage.syncPlayer(player.getUuidAsString(), json);
            }
        });
    }

    public static void overwritePlayer(ServerPlayerEntity player, JsonObject object) {
        PlayerConfigs config = playerCache.get(player);
        for (String s : object.keySet()) {
            config.setValue(new Identifier(s), object.get(s).toString());
        }
    }

    public static void onLoad(ServerPlayerEntity player) {
        ((AdvancementNoDisplay) player.getAdvancementTracker()).setForceAdding(true);
        for (AdvancementEntry entry : player.getEntityWorld().getServer().getAdvancementLoader().getAdvancements()) {
            AdvancementProgress progress = player.getAdvancementTracker().getProgress(entry);
            for (String criterion : progress.getObtainedCriteria())
                player.getAdvancementTracker().revokeCriterion(entry, criterion);

            for (String criterion : progress.getUnobtainedCriteria()) {
                if (hasAdvancement(player, entry.id(), criterion))
                    player.getAdvancementTracker().grantCriterion(entry, criterion);
            }
        }
        player.getAdvancementTracker().sendUpdate(player);
        ((AdvancementNoDisplay) player.getAdvancementTracker()).setForceAdding(false);
    }

    public static String getUrl(String route, ServerPlayerEntity player) {
        return route + "/" + ((PersonatusProfile) player.getGameProfile()).getRealProfile().getId().toString();
    }

    public static String getUrl(String route, GameProfile player) {
        return route + "/" + ((PersonatusProfile) player).getRealProfile().getId().toString();
    }
}
