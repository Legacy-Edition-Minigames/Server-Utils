package net.kyrptonaught.serverutils.customUI;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ScreenConfigLoader implements SimpleSynchronousResourceReloadListener {
    public static final Identifier ID = new Identifier(ServerUtilsMod.MOD_ID, ServerUtilsMod.CustomUIModule.getMOD_ID());

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        CustomUI.reload();
        Map<Identifier, Resource> resources = manager.findResources(ID.getPath(), (identifier) -> identifier.getPath().endsWith(".json") || identifier.getPath().endsWith(".json5"));
        for (Identifier id : resources.keySet()) {
                try (InputStreamReader reader = new InputStreamReader(resources.get(id).getInputStream(), StandardCharsets.UTF_8)) {

                    ScreenConfig screenConfig = ServerUtilsMod.getGson().fromJson(reader, ScreenConfig.class);
                    if (screenConfig == null) {
                        System.out.println(ID + " - Error parsing file: " + id);
                        continue;
                    }

                    String screenID = getID(id, "definitions");
                    if (screenID != null) {
                        CustomUI.addPresets(screenID, screenConfig);
                        continue;
                    }
                    screenID = getID(id, "screens");
                    if (screenID != null) {
                        CustomUI.addScreen(screenID, screenConfig);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }

    private static String getID(Identifier filename, String path) {
        int index = filename.getPath().indexOf(path);
        if (index == -1) return null;
        return filename.getNamespace() + ":" + filename.getPath().substring(index + path.length() + 1, filename.getPath().lastIndexOf("."));
    }
}