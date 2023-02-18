package net.kyrptonaught.serverutils.mixin.customWorldBorder;

import net.kyrptonaught.serverutils.customWorldBorder.CustomWorldBorderArea;
import net.kyrptonaught.serverutils.customWorldBorder.duckInterface.CustomWorldBorder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(WorldBorder.class)
public abstract class WorldBorderMixin implements CustomWorldBorder {

    @Shadow
    private WorldBorder.Area area;

    @Shadow
    public abstract void setCenter(double x, double z);

    @Shadow
    @Final
    private List<WorldBorderListener> listeners;

    @Override
    public void setShape(BlockPos min, BlockPos max) {
        this.listeners.clear();
        double xSize = (max.getX() - min.getX()) / 2D;
        double zSize = (max.getZ() - min.getZ()) / 2D;

        setCenter(min.getX() + xSize, min.getZ() + zSize);
        this.area = new CustomWorldBorderArea((WorldBorder) (Object) this, xSize, zSize);
    }
}
