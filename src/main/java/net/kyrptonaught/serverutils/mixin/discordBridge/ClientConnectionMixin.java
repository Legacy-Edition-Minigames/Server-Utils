package net.kyrptonaught.serverutils.mixin.discordBridge;

import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {

    @Redirect(method = "getAddressAsString", at = @At(value = "INVOKE", target = "Ljava/lang/Object;toString()Ljava/lang/String;"))
    private String hideIP(Object instance) {
        return "<IP>" + instance + "</IP>";
    }
}
