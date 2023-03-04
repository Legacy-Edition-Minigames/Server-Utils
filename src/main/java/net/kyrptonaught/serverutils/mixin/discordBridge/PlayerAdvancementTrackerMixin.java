package net.kyrptonaught.serverutils.mixin.discordBridge;

import net.kyrptonaught.serverutils.discordBridge.MessageSender;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerAdvancementTracker.class)
public class PlayerAdvancementTrackerMixin {

    @Redirect(method = "grantCriterion", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcast(Lnet/minecraft/text/Text;Z)V"))
    public void hijackAdvancementMessage(PlayerManager instance, Text message, boolean overlay) {
        MessageSender.sendAdvancementMessage(message);
        instance.broadcast(message,overlay);
        System.out.println(message);
    }
}
