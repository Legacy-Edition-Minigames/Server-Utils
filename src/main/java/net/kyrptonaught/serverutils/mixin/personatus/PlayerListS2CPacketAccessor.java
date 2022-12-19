package net.kyrptonaught.serverutils.mixin.personatus;

import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(PlayerListS2CPacket.class)
public interface PlayerListS2CPacketAccessor {


    @Mutable
    @Accessor("entries")
    void setEntries(List<PlayerListS2CPacket.Entry> playerNames);

}
