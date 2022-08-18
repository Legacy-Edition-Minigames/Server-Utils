package net.kyrptonaught.serverutils.datapackInteractables;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DatapackLoader implements SimpleSynchronousResourceReloadListener {
    public static final Identifier ID = new Identifier(ServerUtilsMod.MOD_ID, DatapackInteractables.MOD_ID);
    private static final Gson GSON = (new GsonBuilder()).create();

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        System.out.println("reload");
        DatapackInteractables.clear();
        Collection<Identifier> resources = manager.findResources(ID.getPath(), (string) -> string.endsWith(".json"));
        for (Identifier id : resources) {
            if (id.getNamespace().equals(ID.getNamespace()))
                try {
                    JsonObject jsonObj = (JsonObject) JsonParser.parseReader(new InputStreamReader(manager.getResource(id).getInputStream()));

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