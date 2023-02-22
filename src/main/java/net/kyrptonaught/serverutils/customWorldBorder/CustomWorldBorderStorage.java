package net.kyrptonaught.serverutils.customWorldBorder;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

public class CustomWorldBorderStorage extends PersistentState {

    private final CustomWorldBorderManager customWorldBorderManager;

    public CustomWorldBorderStorage() {
        this(new CustomWorldBorderManager());
    }

    public CustomWorldBorderStorage(CustomWorldBorderManager customWorldBorderManager) {
        super();
        this.customWorldBorderManager = customWorldBorderManager;
    }

    public static PersistentState fromNbt(ServerWorld world, NbtCompound tag) {
        boolean enabled = tag.getBoolean("enabled");
        double xCenter = tag.getDouble("xCenter");
        double zCenter = tag.getDouble("zCenter");
        double xSize = tag.getDouble("xSize");
        double zSize = tag.getDouble("zSize");
        double maxY = tag.getDouble("yMax");

        CustomWorldBorderManager manager = new CustomWorldBorderManager();
        manager.setCustomWorldBorder(world, enabled, xCenter, zCenter, xSize, zSize, maxY);
        return new CustomWorldBorderStorage(manager);
    }

    public NbtCompound writeNbt(NbtCompound tag) {
        tag.putBoolean("enabled", customWorldBorderManager.enabled);
        tag.putDouble("xCenter", customWorldBorderManager.xCenter);
        tag.putDouble("zCenter", customWorldBorderManager.zCenter);
        tag.putDouble("xSize", customWorldBorderManager.xSize);
        tag.putDouble("zSize", customWorldBorderManager.zSize);
        tag.putDouble("yMax", customWorldBorderManager.maxY);
        return tag;
    }

    public CustomWorldBorderManager getCustomWorldBorderManager() {
        return customWorldBorderManager;
    }

    @Override
    public boolean isDirty() {
        return true;
    }
}