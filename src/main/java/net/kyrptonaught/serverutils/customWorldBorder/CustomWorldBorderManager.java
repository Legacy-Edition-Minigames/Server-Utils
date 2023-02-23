package net.kyrptonaught.serverutils.customWorldBorder;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.serverutils.customWorldBorder.duckInterface.CustomWorldBorder;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.WorldBorderCenterChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderSizeChangedS2CPacket;
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
    public double maxSize, maxY;

    private final WorldBorder q1Border = new WorldBorder();
    private final WorldBorder q2Border = new WorldBorder();
    private final WorldBorder q3Border = new WorldBorder();
    private final WorldBorder q4Border = new WorldBorder();

    public CustomWorldBorderManager() {
        maxY = maxSize = zSize = xSize = 5.9999968E7;
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

        maxSize = Math.max(xSize, zSize);

        ((CustomWorldBorder) q1Border).setShape(xCenter - xSize + maxSize, zCenter + zSize - maxSize, maxSize * 2);
        ((CustomWorldBorder) q2Border).setShape(xCenter + xSize - maxSize, q1Border.getCenterZ(), maxSize * 2);
        ((CustomWorldBorder) q3Border).setShape(q2Border.getCenterX(), zCenter - zSize + maxSize, maxSize * 2);
        ((CustomWorldBorder) q4Border).setShape(q1Border.getCenterX(), q3Border.getCenterZ(), maxSize * 2);

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

        if (hasLCH(player)) {
            if (!playerBorders.containsKey(player.getUuid())) {
                CustomWorldBorderNetworking.sendCustomWorldBorderPacket(player, xCenter, zCenter, xSize, zSize);
                playerBorders.put(player.getUuid(), new SyncedBorder());
            }
            return;
        }

        SyncedBorder newBorder;
        if (player.getX() >= xCenter) {
            if (player.getZ() >= zCenter) {
                newBorder = new SyncedBorder(q2Border, maxSize * 2);
            } else {
                newBorder = new SyncedBorder(q3Border, maxSize * 2);
            }
        } else if (player.getZ() >= zCenter) {
            newBorder = new SyncedBorder(q1Border, maxSize * 2);
        } else {
            newBorder = new SyncedBorder(q4Border, maxSize * 2);
        }

        SyncedBorder playerBorder = playerBorders.get(player.getUuid());
        if (!newBorder.sizeMatches(playerBorder)) sendSizePacket(player, newBorder.worldBorder);
        if (!newBorder.centerMatches(playerBorder)) sendCenterPacket(player, newBorder.worldBorder);
        playerBorders.put(player.getUuid(), newBorder);
    }

    private static void checkBounds(ServerPlayerEntity player, WorldBorder worldBorder, double maxY) {
        if (player.getX() > worldBorder.getBoundEast())
            player.refreshPositionAfterTeleport(worldBorder.getBoundEast() - .5, player.getY(), player.getZ());
        if (player.getX() < worldBorder.getBoundWest())
            player.refreshPositionAfterTeleport(worldBorder.getBoundWest() + .5, player.getY(), player.getZ());
        if (player.getZ() > worldBorder.getBoundSouth())
            player.refreshPositionAfterTeleport(player.getX(), player.getY(), worldBorder.getBoundSouth() - .5);
        if (player.getZ() < worldBorder.getBoundNorth())
            player.refreshPositionAfterTeleport(player.getX(), player.getY(), worldBorder.getBoundNorth() + .5);
        if (player.getY() > maxY) player.refreshPositionAfterTeleport(player.getX(), maxY - .5, player.getZ());
    }

    public static void sendCenterPacket(ServerPlayerEntity player, WorldBorder worldBorder) {
        player.networkHandler.sendPacket(new WorldBorderCenterChangedS2CPacket(worldBorder));
    }

    public static void sendSizePacket(ServerPlayerEntity player, WorldBorder worldBorder) {
        player.networkHandler.sendPacket(new WorldBorderSizeChangedS2CPacket(worldBorder));
    }

    public static void updateWorldBorderToAll(List<ServerPlayerEntity> players, WorldBorder worldBorder) {
        Packet<?> sizePacket = new WorldBorderSizeChangedS2CPacket(worldBorder);
        Packet<?> centerPacket = new WorldBorderCenterChangedS2CPacket(worldBorder);
        for (ServerPlayerEntity player : players) {
            player.networkHandler.sendPacket(sizePacket);
            player.networkHandler.sendPacket(centerPacket);
        }
    }

    private static boolean hasLCH(ServerPlayerEntity player) {
        return ServerPlayNetworking.canSend(player, CustomWorldBorderNetworking.CUSTOM_BORDER_PACKET);
    }

    public static class SyncedBorder {
        private final WorldBorder worldBorder;
        private final double size;

        public SyncedBorder() {
            this(null, 0);
        }

        public SyncedBorder(WorldBorder worldBorder, double size) {
            this.worldBorder = worldBorder;
            this.size = size;
        }

        public boolean centerMatches(SyncedBorder other) {
            if (other == null) return false;
            return other.worldBorder.getCenterX() == worldBorder.getCenterX() && other.worldBorder.getCenterZ() == worldBorder.getCenterZ();
        }

        public boolean sizeMatches(SyncedBorder other) {
            if (other == null) return false;
            return other.size == size;
        }
    }
}
