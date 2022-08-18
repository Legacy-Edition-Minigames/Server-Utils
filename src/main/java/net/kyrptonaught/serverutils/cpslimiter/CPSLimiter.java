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
    private static final DecimalFormat df = new DecimalFormat("0.0");

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
        float cps = 1 / (playerLastPress.get(uuid).getCPS(smoothing));

        MutableText text = new LiteralText("CPS: " + df.format(cps));

        boolean passing = cps < cpsLimit;
        if (!passing)
            text.formatted(Formatting.RED);

        player.sendMessage(text, true);
        return passing;
    }

    public static CPSLimitConfig getConfig() {
        return (CPSLimitConfig) ServerUtilsMod.configManager.getConfig(MOD_ID);
    }

    public static class ClickTimeStamps {
        long lastPressed = 0;
        float lastDif = 0;

        public ClickTimeStamps() {

        }

        public float getCPS(float smoothing) {
            long timeNow = System.currentTimeMillis();
            if (lastPressed == 0) {
                lastPressed = timeNow;
                return 1;
            }

            float dif = (timeNow - lastPressed) / 1000.0f;

            float curAvg = dif;
            if (lastDif != 0) {
                curAvg = (dif * smoothing) + (lastDif * (1 - smoothing));
            }

            lastDif = dif;
            lastPressed = timeNow;
            return curAvg;
        }
    }
}
