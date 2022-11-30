package net.kyrptonaught.serverutils.mixin.personatus;

import com.mojang.authlib.GameProfile;
import net.kyrptonaught.serverutils.personatus.PersonatusProfile;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    public ServerPlayerEntity player;


    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("HEAD"))
    public void spoofPacket(Packet<?> packet, @Nullable PacketCallbacks callbacks, CallbackInfo ci) {
        if (packet instanceof TeamS2CPacket teamPacket) {
            if (teamPacket.getPlayerNames().contains(this.player.getGameProfile().getName())) {
                List<String> players = new ArrayList<>(teamPacket.getPlayerNames());
                players.add(((PersonatusProfile) this.player.getGameProfile()).getRealProfile().getName());
                ((TeamS2CPacketAccessor) teamPacket).setPlayerNames(players);
            }
        } else if (packet instanceof PlayerListS2CPacket listPacket) {
            GameProfile profile = this.player.getGameProfile();
            List<PlayerListS2CPacket.Entry> entries = listPacket.getEntries();
            for (int i = 0; i < entries.size(); i++) {
                PlayerListS2CPacket.Entry entry = entries.get(i);
                if (profile.equals(entry.getProfile())) {
                    listPacket.getEntries().set(i, new PlayerListS2CPacket.Entry(((PersonatusProfile) profile).getRealProfile(), entry.getLatency(), entry.getGameMode(), entry.getDisplayName(), entry.getPublicKeyData()));
                }
            }
        }
    }
}
