package net.kyrptonaught.serverutils.mixin.editablePlayerNBT;

import net.minecraft.command.EntityDataObject;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(EntityDataObject.class)
public class EntityDataObjectMixin {
    @Final
    @Shadow
    private Entity entity;

    /**
     * @author Lilly Rosaline
     */
    @Inject(at = @At(value = "HEAD"), method = "setNbt", cancellable = true)
    public void injectSetNbt(NbtCompound nbt, CallbackInfo ci) {
        if (this.entity instanceof PlayerEntity) {
            UUID uUID = this.entity.getUuid();
            this.entity.readNbt(nbt);
            this.entity.setUuid(uUID);
            ci.cancel();
        }
    }
}