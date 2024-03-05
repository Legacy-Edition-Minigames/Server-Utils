package net.kyrptonaught.serverutils.datapackInteractables;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.kyrptonaught.serverutils.Module;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.*;

public class DatapackInteractables extends Module {
    private static final List<BlockList> datapackLists = new ArrayList<>();
    private static final HashMap<RegistryKey<World>, Set<Identifier>> worldBlockLists = new HashMap<>();

    public void onInitialize() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new DatapackLoader());

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            parseBlockLists();
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (player.isSpectator()) return ActionResult.PASS;

            Block hitBlock = world.getBlockState(hitResult.getBlockPos()).getBlock();
            if (!isInteractionAllowed(world.getRegistryKey(), Registries.BLOCK.getId(hitBlock)))
                return ActionResult.FAIL;
            return ActionResult.PASS;
        });
    }

    public static boolean isInteractionAllowed(RegistryKey<World> world, Identifier blockID) {
        return !worldBlockLists.get(world).contains(blockID);
    }

    public static void addInitialBlockListConfig(BlockList list) {
        if (list != null)
            datapackLists.add(list);
    }

    public static void unloadWorld(RegistryKey<World> world) {
        worldBlockLists.remove(world);
    }

    public static void addToBlockList(RegistryKey<World> world, BlockList list) {
        if (!worldBlockLists.containsKey(world))
            worldBlockLists.put(world, new HashSet<>(worldBlockLists.get(ServerWorld.OVERWORLD)));

        if (list != null) {
            addToBlockList(worldBlockLists.get(world), list.clear, list.blockIDs);
        }
    }

    private static void addToBlockList(Set<Identifier> blockIDs, boolean clear, Map<String, Boolean> blockIds) {
        if (clear) blockIds.clear();

        for (String id : blockIds.keySet()) {
            if (!id.startsWith("#")) {
                if (blockIds.get(id)) blockIDs.add(new Identifier(id));
                else blockIDs.remove(new Identifier(id));
            } else {
                if (blockIds.get(id))
                    blockIDs.addAll(getBlockIDsInTag(new Identifier(id.replaceAll("#", ""))));
                else
                    blockIDs.removeAll(getBlockIDsInTag(new Identifier(id.replaceAll("#", ""))));
            }
        }
    }

    private static void parseBlockLists() {
        worldBlockLists.put(ServerWorld.OVERWORLD, new HashSet<>());

        for (BlockList list : datapackLists) {
            addToBlockList(ServerWorld.OVERWORLD, list);
        }

        datapackLists.clear();
    }

    private static List<Identifier> getBlockIDsInTag(Identifier blockTagKey) {
        Optional<TagKey<Block>> tag = Registries.BLOCK.streamTags().filter(blockTagKey1 -> blockTagKey1.id().equals(blockTagKey)).findFirst();
        if (tag.isPresent()) {
            List<Identifier> blocks = new ArrayList<>();
            Registries.BLOCK.iterateEntries(tag.get()).forEach(registryEntry -> {
                registryEntry.getKey().ifPresent(registryEntry2 -> blocks.add(registryEntry2.getValue()));
            });
            return blocks;
        }

        return List.of();
    }
}
