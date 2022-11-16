package net.kyrptonaught.serverutils.personatus;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;

public interface ServerLoginNetworkHandlerGetter {

    GameProfile getProfile();

    MinecraftServer getServer();
}
