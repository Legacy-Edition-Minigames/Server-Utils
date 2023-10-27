package net.kyrptonaught.serverutils.customWorldBorder;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.serverutils.customWorldBorder.duckInterface.CustomWorldBorder;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.WorldBorderInitializeS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorder;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CustomWorldBorderManager {
    public HashMap<UUID, SyncedBorder> playerBorders = new HashMap<>();

    public boolean enabled = false;

    public double xCenter = 0, zCenter = 0;
    public double xSize, zSize;
    public double maxY;

    private final WorldBorder q1Border;
    private final WorldBorder q2Border;

    public CustomWorldBorderManager() {
        maxY = zSize = xSize = 5.9999968E7;
        q1Border = new WorldBorder();
        q1Border.setWarningBlocks(0);
        q1Border.setWarningTime(0);
        q1Border.setDamagePerBlock(0);
        q2Border = new WorldBorder();
        q2Border.setWarningBlocks(0);
        q2Border.setWarningTime(0);
        q2Border.setDamagePerBlock(0);
    }

    public void setCustomWorldBorder(ServerWorld world, BlockPos min, BlockPos max) {
        double minX = Math.min(min.getX(), max.getX());
        double maxX = Math.max(min.getX(), max.getX()) + 1;
        double minZ = Math.min(min.getZ(), max.getZ());
        double maxZ = Math.max(min.getZ(), max.getZ()) + 1;

        maxY = Math.max(min.getY(), max.getY()) + 1;
        xSize = (maxX - minX) / 2D;
        zSize = (maxZ - minZ) / 2D;
        xCenter = minX + xSize;
        zCenter = minZ + zSize;

        setCustomWorldBorder(world, true, xCenter, zCenter, xSize, zSize, maxY);
    }

    public void setCustomWorldBorder(ServerWorld world, boolean enabled, double xCenter, double zCenter, double xSize, double zSize, double maxY) {
        playerBorders.clear();

        this.maxY = maxY;
        this.xSize = xSize;
        this.zSize = zSize;
        this.xCenter = xCenter;
        this.zCenter = zCenter;

        ((CustomWorldBorder) q1Border).setShape(xCenter - xSize + zSize, zCenter, zSize * 2);
        ((CustomWorldBorder) q2Border).setShape(xCenter + xSize - zSize, zCenter, zSize * 2);

        ((CustomWorldBorder) world.getWorldBorder()).setShape(xCenter, zCenter, xSize, zSize);
        this.enabled = enabled;
    }

    public void setEnabled(ServerWorld world, boolean enabled) {
        playerBorders.clear();
        this.enabled = enabled;

        if (!enabled) {
            ((CustomWorldBorder) world.getWorldBorder()).setShape(0, 0, 5.9999968E7);
            updateWorldBorderToAll(world.getPlayers(), world.getWorldBorder());
        }
    }

    public void tickPlayer(ServerPlayerEntity player, WorldBorder worldBorder) {
        checkBounds(player, worldBorder, maxY);

        if (!playerBorders.containsKey(player.getUuid()))
            playerBorders.put(player.getUuid(), new SyncedBorder(hasLCH(player), null));

        SyncedBorder previousSync = playerBorders.get(player.getUuid());
        if (previousSync.hasLCH) {
            if (previousSync.lastSyncPositive == null) {
                CustomWorldBorderNetworking.sendCustomWorldBorderPacket(player, xCenter, zCenter, xSize, zSize);
                previousSync.lastSyncPositive = true;
            }
        } else if (player.getX() >= xCenter) {
            if (!previousSync.didPositiveSync()) {
                sendWorldBorderPacket(player, q2Border);
                previousSync.lastSyncPositive = true;
            }
        } else {
            if (!previousSync.didNegativeSync()) {
                sendWorldBorderPacket(player, q1Border);
                previousSync.lastSyncPositive = false;
            }
        }
    }

    private static void checkBounds(ServerPlayerEntity player, WorldBorder worldBorder, double maxY) {
        if (player.getX() > worldBorder.getBoundEast())
            player.teleport(worldBorder.getBoundEast() - .5, player.getY(), player.getZ());
        if (player.getX() < worldBorder.getBoundWest())
            player.teleport(worldBorder.getBoundWest() + .5, player.getY(), player.getZ());
        if (player.getZ() > worldBorder.getBoundSouth())
            player.teleport(player.getX(), player.getY(), worldBorder.getBoundSouth() - .5);
        if (player.getZ() < worldBorder.getBoundNorth())
            player.teleport(player.getX(), player.getY(), worldBorder.getBoundNorth() + .5);
        if (player.getY() > maxY)
            player.teleport(player.getX(), maxY - .5, player.getZ());
    }

    public static void sendWorldBorderPacket(ServerPlayerEntity player, WorldBorder worldBorder) {
        player.networkHandler.sendPacket(new WorldBorderInitializeS2CPacket(worldBorder));
    }


    public static void updateWorldBorderToAll(List<ServerPlayerEntity> players, WorldBorder worldBorder) {
        Packet<?> packet = new WorldBorderInitializeS2CPacket(worldBorder);
        for (ServerPlayerEntity player : players) {
            player.networkHandler.sendPacket(packet);
        }
    }

    private static boolean hasLCH(ServerPlayerEntity player) {
        return ServerPlayNetworking.canSend(player, CustomWorldBorderNetworking.CUSTOM_BORDER_PACKET);
    }

    public static class SyncedBorder {
        public boolean hasLCH;
        public Boolean lastSyncPositive;

        public SyncedBorder(boolean hasLCH, Boolean lastSyncPositive) {
            this.hasLCH = hasLCH;
            this.lastSyncPositive = lastSyncPositive;
        }

        public boolean didPositiveSync() {
            return lastSyncPositive != null && lastSyncPositive;
        }

        public boolean didNegativeSync() {
            return lastSyncPositive != null && !lastSyncPositive;
        }
    }
}
