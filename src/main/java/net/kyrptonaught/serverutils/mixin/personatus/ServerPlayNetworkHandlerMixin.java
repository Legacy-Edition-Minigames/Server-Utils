package net.kyrptonaught.serverutils.mixin.personatus;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.ParseResults;
import io.netty.buffer.Unpooled;
import net.kyrptonaught.serverutils.personatus.PacketCopier;
import net.kyrptonaught.serverutils.personatus.PersonatusProfile;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
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


    @Shadow
    @Final
    public ClientConnection connection;

    @Shadow protected abstract ParseResults<ServerCommandSource> parse(String command);

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("HEAD"), cancellable = true)
    public void spoofPacket(Packet<?> packet, @Nullable PacketCallbacks callbacks, CallbackInfo ci) {
        GameProfile profile = this.player.getGameProfile();
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
                    if (profile.getName().equals(entry.getProfile().getName())) {
                        entries.set(i, new PlayerListS2CPacket.Entry(spoofProfile.getRealProfile(), entry.getLatency(), entry.getGameMode(), entry.getDisplayName(), entry.getPublicKeyData()));
                    }
                }

                connection.send(listPacket);
                ci.cancel();
            }
        }
    }
}
