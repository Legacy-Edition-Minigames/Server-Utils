package net.kyrptonaught.serverutils.cpslimiter;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;

public class CPSLimiter {
    public static String MOD_ID = "cpslimiter";

    public static HashMap<UUID, ClickTimeStamps> playerLastPress = new HashMap<>();

    private static float smoothing, cpsLimit;

    public static void onInitialize() {
        ServerUtilsMod.configManager.registerFile(MOD_ID, new CPSLimitConfig());
        ServerUtilsMod.configManager.load();
        smoothing = (float) getConfig().smoothing;
        cpsLimit = (float) getConfig().CPSLimit;

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

    public static CPSLimitConfig getConfig() {
        return (CPSLimitConfig) ServerUtilsMod.configManager.getConfig(MOD_ID);
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
