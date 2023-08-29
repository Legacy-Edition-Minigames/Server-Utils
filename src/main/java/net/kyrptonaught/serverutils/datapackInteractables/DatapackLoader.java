package net.kyrptonaught.serverutils.datapackInteractables;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DatapackLoader implements SimpleSynchronousResourceReloadListener {
    public static final Identifier ID = new Identifier(ServerUtilsMod.MOD_ID, ServerUtilsMod.DatapackInteractablesModule.getMOD_ID());

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        DatapackInteractables.clear();
        Map<Identifier, Resource> resources = manager.findResources(ID.getPath(), (identifier) -> identifier.getPath().endsWith(".json") || identifier.getPath().endsWith(".json5"));
        for (Identifier id : resources.keySet()) {
            try (InputStreamReader reader = new InputStreamReader(resources.get(id).getInputStream(), StandardCharsets.UTF_8)) {

                BlockList blockList = ServerUtilsMod.getGson().fromJson(reader, BlockList.class);
                if (blockList == null) {
                    System.out.println(ID + " - Error parsing file: " + id);
                    continue;
                }
                DatapackInteractables.addBlockList(blockList.isWhitelist, blockList.blockIDs);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class BlockList {
        public boolean isWhitelist = true;
        public Set<String> blockIDs = new HashSet<>();
    }
}