package net.kyrptonaught.serverutils.mixin.whitelistSync;

import com.mojang.authlib.GameProfile;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.SocketAddress;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    @Inject(method = "checkCanJoin", at = @At(target = "Lnet/minecraft/text/Text;translatable(Ljava/lang/String;)Lnet/minecraft/text/MutableText;", value = "INVOKE", ordinal = 0), cancellable = true)
    public void spoofWhitelistKickMSG(SocketAddress address, GameProfile profile, CallbackInfoReturnable<Text> cir) {
        String spoof = ServerUtilsMod.whitelistSyncMod.getConfig().whitelistKickMSG;
        if (spoof != null && !spoof.isEmpty()) cir.setReturnValue(Text.literal(spoof));
    }
}
