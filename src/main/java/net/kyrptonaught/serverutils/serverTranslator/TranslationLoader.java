package net.kyrptonaught.serverutils.serverTranslator;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class TranslationLoader implements SimpleSynchronousResourceReloadListener {
    public static final Identifier ID = new Identifier(ServerUtilsMod.MOD_ID, ServerUtilsMod.ServerTranslatorModule.getMOD_ID());

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        TranslationStorage.clear();

        Map<Identifier, Resource> resources = manager.findResources(ID.getPath(), (identifier) -> identifier.getPath().endsWith(".json") || identifier.getPath().endsWith(".json5"));
        for (Identifier id : resources.keySet()) {
            try (InputStreamReader reader = new InputStreamReader(resources.get(id).getInputStream(), StandardCharsets.UTF_8)) {

                JsonObject languageStorage = ServerUtilsMod.getGson().fromJson(reader, JsonObject.class);
                if (languageStorage == null) {
                    System.out.println(ID + " - Error parsing file: " + id);
                    continue;
                }

                String lang = getRawFileName(id.getPath());

                for (String key : languageStorage.keySet()) {
                    TranslationStorage.addTranslation(lang, key, languageStorage.get(key).getAsString());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String getRawFileName(String fileName) {
        return fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf("."));
    }
}