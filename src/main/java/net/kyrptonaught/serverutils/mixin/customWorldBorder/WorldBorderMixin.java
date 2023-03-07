package net.kyrptonaught.serverutils.mixin.customWorldBorder;

import net.kyrptonaught.serverutils.customWorldBorder.CustomWorldBorderArea;
import net.kyrptonaught.serverutils.customWorldBorder.duckInterface.CustomWorldBorder;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(WorldBorder.class)
public abstract class WorldBorderMixin implements CustomWorldBorder {

    @Shadow
    private WorldBorder.Area area;

    @Shadow
    public abstract void setCenter(double x, double z);

    @Shadow
    public abstract void setSize(double size);

    @Shadow
    @Final
    private List<WorldBorderListener> listeners;

    @Override
    public void setShape(double xCenter, double zCenter, double xSize, double zSize) {
        this.listeners.clear();
        setCenter(xCenter, zCenter);
        this.area = new CustomWorldBorderArea((WorldBorder) (Object) this, xSize, zSize);
    }

    @Override
    public void setShape(double xCenter, double zCenter, double size) {
        setCenter(xCenter, zCenter);
        setSize(size);
    }

    @Inject(method = "setSize", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/border/WorldBorder;getListeners()Ljava/util/List;"), cancellable = true)
    public void DontUpdateSize(double size, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "setCenter", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/border/WorldBorder;getListeners()Ljava/util/List;"), cancellable = true)
    public void DontUpdateCenter(double x, double z, CallbackInfo ci) {
        ci.cancel();
    }
}
