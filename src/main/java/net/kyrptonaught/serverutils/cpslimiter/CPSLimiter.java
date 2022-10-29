package net.kyrptonaught.serverutils.cpslimiter;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyrptonaught.serverutils.ModuleWConfig;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.UUID;

public class CPSLimiter extends ModuleWConfig<CPSLimitConfig> {
    public static HashMap<UUID, ClickTimeStamps> playerLastPress = new HashMap<>();

    private static float smoothing, cpsLimit;

    @Override
    public void onConfigLoad(CPSLimitConfig config) {
        smoothing = (float) config.smoothing;
        cpsLimit = (float) config.CPSLimit;
    }

    @Override
    public CPSLimitConfig createDefaultConfig() {
        return new CPSLimitConfig();
    }

    @Override
    public void onInitialize() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            playerLastPress.putIfAbsent(handler.player.getUuid(), new ClickTimeStamps());
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            playerLastPress.remove(handler.player.getUuid());
        });
        // summon entity for testing: /summon pig ~ ~ ~ {NoAI: 1b,Silent:1b,PersistenceRequired:1b,Invulnerable:1b}
    }

    public static boolean isValidCPS(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();

        return playerLastPress.get(uuid).isPassing(smoothing, cpsLimit);
    }

    public static class ClickTimeStamps {
        long lastPressed = 0;
        float lastDif = 0;

        public boolean isPassing(float smoothing, float limit) {
            long timeNow = System.currentTimeMillis();
            if (lastPressed == 0) {
                lastPressed = timeNow;
                return true;
            }

            float dif = (timeNow - lastPressed) / 1000.0f;

            float curAvg = dif;
            if (lastDif != 0) {
                curAvg = (dif * smoothing) + (lastDif * (1 - smoothing));
            }

            float cps = 1 / curAvg;
            if (cps < limit) {
                lastDif = dif;
                lastPressed = timeNow;
                return true;
            }

            return false;
        }
    }
}
