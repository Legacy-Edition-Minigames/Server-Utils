package net.kyrptonaught.serverutils.mixin.prohibitor;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.KickCommand;
import net.minecraft.server.command.MeCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TeamMsgCommand;
import net.minecraft.server.dedicated.command.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {
        WhitelistCommand.class,
        BanCommand.class,
        BanIpCommand.class,
        BanListCommand.class,
        PardonCommand.class,
        KickCommand.class,
        PardonIpCommand.class,
        TeamMsgCommand.class,
        MeCommand.class
})
public class RemoveCommandsMixin {

    @Inject(method = "register", at = @At("HEAD"), cancellable = true, require = 0)
    private static void skip(CommandDispatcher<ServerCommandSource> dispatcher, CallbackInfo ci) {
        ci.cancel();
    }
}
