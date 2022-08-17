package net.kyrptonaught.serverutils.cpslimiter;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.brandBlocker.BrandBlockerConfig;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CPSLimiter {
    public static String MOD_ID = "cpslimiter";

    public static HashMap<UUID, ClickTimeStamps> playerLastPress = new HashMap<>();
    private static final DecimalFormat df = new DecimalFormat("0.0");

    public static void onInitialize() {
        ServerUtilsMod.configManager.registerFile(MOD_ID, new CPSLimitConfig());

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            UUID uuid = handler.player.getUuid();
            if (!playerLastPress.containsKey(uuid))
                playerLastPress.put(uuid, new ClickTimeStamps());
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID uuid = handler.player.getUuid();
            playerLastPress.remove(uuid);
        });
    }

    public static boolean isValidCPS(ServerPlayerEntity player) {
        long current = System.currentTimeMillis();
        UUID uuid = player.getUuid();
        if (!playerLastPress.containsKey(uuid))
            playerLastPress.put(uuid, new ClickTimeStamps());

        float cps = playerLastPress.get(uuid).getCPS(current, getConfig().smoothing);
        MutableText text = new LiteralText("CPS: " + df.format(1.0 / cps));

        if(cps > getConfig().CPSLimit)
            text.formatted(Formatting.RED);

        player.sendMessage(text, true);

        return cps < getConfig().CPSLimit;
    }

    public static CPSLimitConfig getConfig() {
        return (CPSLimitConfig) ServerUtilsMod.configManager.getConfig(MOD_ID);
    }

    public static class ClickTimeStamps {
        long lastPressed = 0;
        float lastDif = 0;

        public ClickTimeStamps() {

        }

        public float getCPS(long timeNow, float smoothing) {
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
