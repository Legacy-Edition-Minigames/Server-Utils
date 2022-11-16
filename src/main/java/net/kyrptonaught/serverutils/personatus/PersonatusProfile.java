package net.kyrptonaught.serverutils.personatus;

import com.mojang.authlib.GameProfile;

public interface PersonatusProfile {

    GameProfile getRealProfile();

    GameProfile getSpoofProfile();

    void setRealProfile(GameProfile realProfile);

    default boolean isSpoofed() {
        return !getRealProfile().equals(getSpoofProfile());
    }

}
