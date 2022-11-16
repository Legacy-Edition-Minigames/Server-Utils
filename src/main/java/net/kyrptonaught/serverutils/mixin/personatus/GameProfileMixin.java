package net.kyrptonaught.serverutils.mixin.personatus;

import com.mojang.authlib.GameProfile;
import net.kyrptonaught.serverutils.personatus.PersonatusProfile;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameProfile.class)
public class GameProfileMixin implements PersonatusProfile {

    private GameProfile realProfile;

    @Override
    public GameProfile getRealProfile() {
        if (realProfile == null)
            return getSpoofProfile();
        return realProfile;
    }

    @Override
    public GameProfile getSpoofProfile() {
        return (GameProfile) (Object) this;
    }

    @Override
    public void setRealProfile(GameProfile realProfile) {
        this.realProfile = realProfile;
    }
}
