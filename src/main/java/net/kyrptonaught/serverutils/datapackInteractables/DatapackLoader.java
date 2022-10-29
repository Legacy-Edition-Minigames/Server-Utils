package net.kyrptonaught.serverutils.datapackInteractables;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DatapackLoader implements SimpleSynchronousResourceReloadListener {
    public static final Identifier ID = new Identifier(ServerUtilsMod.MOD_ID, ServerUtilsMod.DatapackInteractablesModule.getMOD_ID());
    private static final Gson GSON = (new GsonBuilder()).create();

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        DatapackInteractables.clear();
        Map<Identifier, Resource> resources = manager.findResources(ID.getPath(), (identifier) -> identifier.getPath().endsWith(".json"));
        for (Identifier id : resources.keySet()) {
            if (id.getNamespace().equals(ID.getNamespace()))
                try {
                    JsonObject jsonObj = (JsonObject) JsonParser.parseReader(new InputStreamReader(resources.get(id).getInputStream()));

                    BlockList blockList = GSON.fromJson(jsonObj, BlockList.class);
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