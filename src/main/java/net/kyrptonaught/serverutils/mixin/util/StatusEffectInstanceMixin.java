package net.kyrptonaught.serverutils.mixin.util;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatusEffectInstance.class)
public abstract class StatusEffectInstanceMixin {

    @Shadow
    public abstract int getAmplifier();

    @Shadow
    @Final
    private static String AMPLIFIER_NBT_KEY;

    @Inject(method = "writeTypelessNbt", at = @At("TAIL"))
    private void writeIntAmp(NbtCompound nbt, CallbackInfo ci) {
        nbt.putInt(AMPLIFIER_NBT_KEY + "_int", this.getAmplifier());
    }

    @Redirect(method = "fromNbt(Lnet/minecraft/entity/effect/StatusEffect;Lnet/minecraft/nbt/NbtCompound;)Lnet/minecraft/entity/effect/StatusEffectInstance;", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I"))
    private static int readIntAmp(int a, int b, StatusEffect type, NbtCompound nbt) {
        if (nbt.contains(AMPLIFIER_NBT_KEY + "_int")) {
            return Math.max(nbt.getInt(AMPLIFIER_NBT_KEY + "_int"), b);
        }

        return Math.max(a, b);
    }
}
