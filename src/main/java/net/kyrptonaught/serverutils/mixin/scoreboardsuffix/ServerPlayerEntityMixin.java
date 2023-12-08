package net.kyrptonaught.serverutils.mixin.scoreboardsuffix;

import com.mojang.authlib.GameProfile;
import net.kyrptonaught.serverutils.scoreboardsuffix.ScoreboardSuffixMod;
import net.kyrptonaught.serverutils.scoreboardsuffix.SuffixFormat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    @Shadow
    @Final
    public MinecraftServer server;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "getPlayerListName", at = @At("RETURN"), cancellable = true)
    private void updatePlayerNameWithScore(CallbackInfoReturnable<Text> cir) {
        Text playerName = cir.getReturnValue();
        if (playerName == null) playerName = this.getName();

        Scoreboard scoreboard = this.server.getScoreboard();
        String player = this.getName().getString();
        Team team = scoreboard.getScoreHolderTeam(player);
        MutableText suffix = Team.decorateName(team, playerName);
        ScoreboardSuffixMod.playerSuffixStorage.suffixFormat.scoreboardSuffixes.forEach(newSuffix -> {
            if (newSuffix instanceof SuffixFormat.ScoreboardSuffix) {
                String scoreboardName = newSuffix.suffix;
                int score = scoreboard.getOrCreateScore(ScoreHolder.fromName(player), scoreboard.getNullableObjective(scoreboardName)).getScore();
                ((SuffixFormat.ScoreboardSuffix) newSuffix).updateText(score);
            }

            Style style = newSuffix.displayText.getStyle();
            String fontPlaceHolder = style.getFont().toString();
            style = style.withFont(new Identifier(ScoreboardSuffixMod.playerSuffixStorage.getFont(player, fontPlaceHolder)));
            suffix.append(newSuffix.displayText.copy().setStyle(style));

        });
        cir.setReturnValue(suffix);
    }
}
