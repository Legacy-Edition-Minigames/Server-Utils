package net.kyrptonaught.serverutils.mixin.personatus;

import com.mojang.authlib.GameProfile;
import net.kyrptonaught.serverutils.personatus.PacketCopier;
import net.kyrptonaught.serverutils.personatus.PersonatusProfile;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerCommonNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {

    @Shadow
    protected abstract GameProfile getProfile();

    @Shadow
    @Final
    protected ClientConnection connection;

    @Inject(method = "send", at = @At("HEAD"), cancellable = true)
    public void spoofPacket(Packet<?> packet, @Nullable PacketCallbacks callbacks, CallbackInfo ci) {
        GameProfile profile = this.getProfile();
        PersonatusProfile spoofProfile = (PersonatusProfile) profile;
        if (spoofProfile.isSpoofed()) {
            if (packet instanceof TeamS2CPacket) {
                TeamS2CPacket teamPacket = PacketCopier.copyTeamPacket((TeamS2CPacket) packet);

                List<String> players = new ArrayList<>(teamPacket.getPlayerNames());
                for (int i = 0; i < players.size(); i++) {
                    if (players.get(i).equals(profile.getName()))
                        players.set(i, spoofProfile.getRealProfile().getName());
                }
                ((TeamS2CPacketAccessor) teamPacket).setPlayerNames(players);

                connection.send(teamPacket);
                ci.cancel();
            } else if (packet instanceof PlayerListS2CPacket) {
                PlayerListS2CPacket listPacket = PacketCopier.copyPlayerList((PlayerListS2CPacket) packet);
                List<PlayerListS2CPacket.Entry> entries = listPacket.getEntries();

                for (int i = 0; i < entries.size(); i++) {
                    PlayerListS2CPacket.Entry entry = entries.get(i);
                    if (profile.getName().equals(entry.profile().getName())) {
                        entries.set(i, new PlayerListS2CPacket.Entry(spoofProfile.getRealProfile().getId(), spoofProfile.getRealProfile(), entry.listed(), entry.latency(), entry.gameMode(), entry.displayName(), entry.chatSession()));
                    }
                }

                connection.send(listPacket);
                ci.cancel();
            } else if (packet instanceof ChatMessageS2CPacket chatPacket) {
                if (chatPacket.sender().equals(spoofProfile.getSpoofProfile().getId())) {
                    connection.send(PacketCopier.copyChatPacket(chatPacket, spoofProfile.getRealProfile().getId()));
                    ci.cancel();
                }
            }

        }
    }
}
