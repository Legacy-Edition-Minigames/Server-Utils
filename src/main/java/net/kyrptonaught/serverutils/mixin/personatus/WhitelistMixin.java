package net.kyrptonaught.serverutils.mixin.personatus;

import com.mojang.authlib.GameProfile;
import net.kyrptonaught.serverutils.personatus.PersonatusProfile;
import net.minecraft.server.ServerConfigList;
import net.minecraft.server.Whitelist;
import net.minecraft.server.WhitelistEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

@Mixin(Whitelist.class)
public abstract class WhitelistMixin extends ServerConfigList<GameProfile, WhitelistEntry> {

    public WhitelistMixin(File file) {
        super(file);
    }

    @Unique
    @Inject(method = "isAllowed", at = @At("HEAD"), cancellable = true)
    public void isAllowed(GameProfile profile, CallbackInfoReturnable<Boolean> cir) {
        GameProfile real = ((PersonatusProfile) profile).getRealProfile();
        cir.setReturnValue(this.contains(real));
    }
}
