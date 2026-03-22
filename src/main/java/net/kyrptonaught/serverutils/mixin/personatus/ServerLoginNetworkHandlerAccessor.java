package net.kyrptonaught.serverutils.mixin.personatus;

import net.fabricmc.fabric.impl.networking.server.ServerLoginNetworkAddon;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerLoginNetworkAddon.class)
public interface ServerLoginNetworkHandlerAccessor {

    @Accessor("server")
    MinecraftServer getServer();
}
