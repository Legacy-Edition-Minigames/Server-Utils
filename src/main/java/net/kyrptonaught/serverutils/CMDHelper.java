package net.kyrptonaught.serverutils;

import net.minecraft.entity.player.PlayerEntity;

public class CMDHelper {

    public static void executeAs(PlayerEntity player, String cmd) {
        player.getServer().getCommandManager().executeWithPrefix(player.getCommandSource().withLevel(2).withSilent(), cmd);
    }
}
