package net.kyrptonaught.serverutils.serverTranslator;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.kyrptonaught.serverutils.Module;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.UUID;

public class ServerTranslator extends Module {

    private static final HashMap<UUID, String> playerLanguages = new HashMap<>();

    public static void registerPlayerLanguage(ServerPlayerEntity player, String language) {
        playerLanguages.put(player.getUuid(), language);
    }

    public static String getLanguage(ServerPlayerEntity player) {
        if (player == null || playerLanguages.containsKey(player.getUuid()))
            return playerLanguages.get(player.getUuid());
        return "en_us";
    }

    public static String translate(ServerPlayerEntity player, String key) {
        return TranslationStorage.getTranslationFor(getLanguage(player), key);
    }

    public static String translate(String key) {
        return translate(null, key);
    }

    @Override
    public void onInitialize() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new TranslationLoader());
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new InjectedLoader());
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> playerLanguages.remove(handler.player.getUuid()));
    }
}
