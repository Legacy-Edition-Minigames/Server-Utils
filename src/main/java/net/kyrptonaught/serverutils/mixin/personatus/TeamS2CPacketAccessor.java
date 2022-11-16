package net.kyrptonaught.serverutils.mixin.personatus;

import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Collection;

@Mixin(TeamS2CPacket.class)
public interface TeamS2CPacketAccessor {

    @Mutable
    @Accessor("playerNames")
    void setPlayerNames(Collection<String> playerNames);
}
