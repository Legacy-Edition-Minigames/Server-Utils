package net.kyrptonaught.serverutils.mixin.brandblocker;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    /*
    @Final
    @Shadow
    public ClientConnection connection;
    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onCustomPayload", at = @At("HEAD"))
    public void onCustomPayload(CustomPayloadC2SPacket packet, CallbackInfo ci) {
        if (packet.getChannel().equals(CustomPayloadC2SPacket.BRAND)) {
            Text msg = BrandBlocker.getConfig().isBlockedBrand(packet.getData().readString());
            if (msg != null) {
                System.out.println("[BrandBlock] Kicked " + player.getEntityName() + "[" + connection.getAddress() + "] for using a blocked client brand");
                ByteBufDataOutput output = new ByteBufDataOutput(new PacketByteBuf(Unpooled.buffer()));

                output.writeUTF("KickPlayer");
                output.writeUTF(player.getEntityName());
                output.writeUTF(msg.asString());
                //ServerPlayNetworking.send(player, VelocityServerSwitchMod.BUNGEECORD_ID, output.getBuf());
                //connection.send(new DisconnectS2CPacket(msg));
                //connection.disconnect(msg);
            }
        }
    }
     */
}
