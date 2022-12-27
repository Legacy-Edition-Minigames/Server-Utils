package net.kyrptonaught.serverutils.mixin.starlightSilencer;

import ca.spottedleaf.starlight.common.util.SaveUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(SaveUtil.class)
public class SaveUtilMixin {


    @Inject(method = "loadLightHook", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Throwable;)V"), cancellable = true)
    private static void silenceLoadError(World world, ChunkPos pos, NbtCompound tag, Chunk into, CallbackInfo ci) {
        ci.cancel();
    }
}
