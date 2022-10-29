package net.kyrptonaught.serverutils.datapackInteractables;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.kyrptonaught.serverutils.Module;
import net.minecraft.block.Block;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Set;
import java.util.stream.Collectors;

public class DatapackInteractables extends Module {
    private static boolean isWhiteList;
    private static Set<Identifier> blockIds;

    public void onInitialize() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new DatapackLoader());
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player.isSpectator()) return ActionResult.PASS;

            Block hitBlock = world.getBlockState(hitResult.getBlockPos()).getBlock();
            if (!isInteractionAllowed(Registry.BLOCK.getId(hitBlock)))
                return ActionResult.FAIL;
            return ActionResult.PASS;
        });
    }

    public static void addBlockList(boolean isWhiteList, Set<String> blockIds) {
        DatapackInteractables.isWhiteList = isWhiteList;
        DatapackInteractables.blockIds = blockIds.stream().map(Identifier::new).collect(Collectors.toSet());
    }

    public static boolean isInteractionAllowed(Identifier blockID) {
        if (blockIds == null) return true;
        return isWhiteList == blockIds.contains(blockID);
    }

    public static void clear() {
        blockIds = null;
    }
}
