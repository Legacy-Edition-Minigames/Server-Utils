package net.kyrptonaught.serverutils.customWorldBorder;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kyrptonaught.serverutils.customWorldBorder.duckInterface.CustomWorldBorder;
import net.minecraft.network.packet.s2c.play.WorldBorderCenterChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderSizeChangedS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorder;

import java.util.HashMap;
import java.util.UUID;

public class CustomWorldBorderManager {
    public static HashMap<UUID, SyncedBorder> playerBorders = new HashMap<>();

    private static double xCenter, zCenter;
    private static double xSize, zSize;
    private static double maxSize, maxY;

    private static final WorldBorder q1Border = new WorldBorder();
    private static final WorldBorder q2Border = new WorldBorder();
    private static final WorldBorder q3Border = new WorldBorder();
    private static final WorldBorder q4Border = new WorldBorder();

    public static void setCustomWorldBorder(ServerWorld world, BlockPos min, BlockPos max) {
        double minX = Math.min(min.getX(), max.getX());
        double maxX = Math.max(min.getX(), max.getX()) + 1;
        double minZ = Math.min(min.getZ(), max.getZ());
        double maxZ = Math.max(min.getZ(), max.getZ()) + 1;
        maxY = Math.max(min.getY(), max.getY()) + 1;

        xSize = (maxX - minX) / 2D;
        zSize = (maxZ - minZ) / 2D;
        xCenter = min.getX() + xSize;
        zCenter = min.getZ() + zSize;

        maxSize = Math.max(xSize, zSize);

        q1Border.setSize(maxSize * 2);
        q2Border.setSize(maxSize * 2);
        q3Border.setSize(maxSize * 2);
        q4Border.setSize(maxSize * 2);

        q1Border.setCenter(xCenter - xSize + maxSize, zCenter + zSize - maxSize);
        q2Border.setCenter(xCenter + xSize - maxSize, q1Border.getCenterZ());
        q3Border.setCenter(q2Border.getCenterX(), zCenter - zSize + maxSize);
        q4Border.setCenter(q1Border.getCenterX(), q3Border.getCenterZ());

        ((CustomWorldBorder) world.getWorldBorder()).setShape(xCenter, zCenter, xSize, zSize);
        playerBorders.clear();
    }

    public static void tickPlayers() {
        ServerTickEvents.START_WORLD_TICK.register(world -> {
            WorldBorder mainBorder = world.getWorldBorder();
            for (ServerPlayerEntity player : world.getPlayers()) {
                checkBounds(player, mainBorder);
                if (hasLCH(player)) {
                    if (!playerBorders.containsKey(player.getUuid())) {
                        CustomWorldBorderNetworking.sendCustomWorldBorderPacket(player, xCenter, zCenter, xSize, zSize);
                        playerBorders.put(player.getUuid(), new SyncedBorder());
                    }
                    continue;
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
        });
    }

    private static void checkBounds(ServerPlayerEntity player, WorldBorder worldBorder) {
        if (player.getX() > worldBorder.getBoundEast()) player.refreshPositionAfterTeleport(worldBorder.getBoundEast() - .5, player.getY(), player.getZ());
        if (player.getX() < worldBorder.getBoundWest()) player.refreshPositionAfterTeleport(worldBorder.getBoundWest() + .5, player.getY(), player.getZ());
        if (player.getZ() > worldBorder.getBoundSouth()) player.refreshPositionAfterTeleport(player.getX(), player.getY(), worldBorder.getBoundSouth() - .5);
        if (player.getZ() < worldBorder.getBoundNorth()) player.refreshPositionAfterTeleport(player.getX(), player.getY(), worldBorder.getBoundNorth() + .5);
        if (player.getY() > maxY) player.refreshPositionAfterTeleport(player.getX(), maxY - .5, player.getZ());
    }

    private static void sendCenterPacket(ServerPlayerEntity player, WorldBorder worldBorder) {
        player.networkHandler.sendPacket(new WorldBorderCenterChangedS2CPacket(worldBorder));
    }

    private static void sendSizePacket(ServerPlayerEntity player, WorldBorder worldBorder) {
        player.networkHandler.sendPacket(new WorldBorderSizeChangedS2CPacket(worldBorder));
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