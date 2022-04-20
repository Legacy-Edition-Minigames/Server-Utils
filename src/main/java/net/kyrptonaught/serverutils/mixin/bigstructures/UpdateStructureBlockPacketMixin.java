package net.kyrptonaught.serverutils.mixin.bigstructures;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.UpdateStructureBlockC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(UpdateStructureBlockC2SPacket.class)
public abstract class UpdateStructureBlockPacketMixin {
    @Mutable
    @Shadow
    @Final
    private Vec3i size;

    @Mutable
    @Shadow
    @Final
    private BlockPos offset;

    @Inject(method = "write", at = @At(value = "TAIL"))
    private void writeIntNotByte(PacketByteBuf buf, CallbackInfo ci) {
        buf.writeInt(this.offset.getX());
        buf.writeInt(this.offset.getY());
        buf.writeInt(this.offset.getZ());
        buf.writeInt(this.size.getX());
        buf.writeInt(this.size.getY());
        buf.writeInt(this.size.getZ());
    }

    @Inject(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At("TAIL"))
    public void readIntNotByte(PacketByteBuf buf, CallbackInfo ci) {
        if (buf.readerIndex() < buf.writerIndex()) {
            this.offset = new BlockPos(MathHelper.clamp(buf.readInt(), -512, 512), MathHelper.clamp(buf.readInt(), -512, 512), MathHelper.clamp(buf.readInt(), -512, 512));
            this.size = new Vec3i(MathHelper.clamp(buf.readInt(), 0, 512), MathHelper.clamp(buf.readInt(), 0, 512), MathHelper.clamp(buf.readInt(), 0, 512));
        }
    }
}