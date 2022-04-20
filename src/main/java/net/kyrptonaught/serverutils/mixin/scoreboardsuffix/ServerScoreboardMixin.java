package net.kyrptonaught.serverutils.mixin.scoreboardsuffix;

import net.kyrptonaught.serverutils.scoreboardsuffix.ScoreboardSuffixMod;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerScoreboard.class)
public abstract class ServerScoreboardMixin extends Scoreboard {

    @Shadow
    @Final
    private MinecraftServer server;

    @Inject(method = "updateScore", at = @At("TAIL"))
    public void updateTeamSuffix(ScoreboardPlayerScore score, CallbackInfo ci) {
        String name = score.getObjective() != null ? score.getObjective().getName() : "";
        if (ScoreboardSuffixMod.playerSuffixStorage != null && ScoreboardSuffixMod.playerSuffixStorage.suffixFormat != null && ScoreboardSuffixMod.playerSuffixStorage.suffixFormat.scoreboardNames.contains(name)) {
            ServerPlayerEntity player = this.server.getPlayerManager().getPlayer(score.getPlayerName());

            PlayerListS2CPacket packet = new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, player);
            this.server.getPlayerManager().sendToAll(packet);
        }
    }
}
