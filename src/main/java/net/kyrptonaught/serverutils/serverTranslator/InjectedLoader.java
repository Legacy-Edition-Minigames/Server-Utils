package net.kyrptonaught.serverutils.serverTranslator;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.TextVisitFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

public class InjectedLoader implements SimpleSynchronousResourceReloadListener {
    public static final Identifier ID = new Identifier(ServerUtilsMod.MOD_ID, "servertranslator_injector");

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public void reload(ResourceManager manager) {
        HashMap<String,String> builder = new HashMap<>();
        BiConsumer<String, String> biConsumer = builder::put;

        try (InputStream inputStream = Language.class.getResourceAsStream("/assets/minecraft/lang/en_us.json")) {
            Language.load(inputStream, biConsumer);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<Identifier, Resource> resources = manager.findResources(ID.getPath(), (identifier) -> identifier.getPath().endsWith(".json") || identifier.getPath().endsWith(".json5"));
        for (Identifier id : resources.keySet()) {
            if (getRawFileName(id.getPath()).equals("en_us"))
                try (InputStream inputStream = resources.get(id).getInputStream()) {
                    Language.load(inputStream, biConsumer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }

        builder.putAll(ServerUtilsMod.ServerTranslatorModule.getConfig().injects);

        final ImmutableMap<String, String> map = ImmutableMap.copyOf(builder);
        Language.setInstance(new Language() {

            @Override
            public String get(String key, String fallback) {
                return map.getOrDefault(key, fallback);
            }

            @Override
            public boolean hasTranslation(String key) {
                return map.containsKey(key);
            }

            @Override
            public boolean isRightToLeft() {
                return false;
            }

            @Override
            public OrderedText reorder(StringVisitable text) {
                return visitor -> text.visit((style, string) -> TextVisitFactory.visitFormatted(string, style, visitor) ? Optional.empty() : StringVisitable.TERMINATE_VISIT, Style.EMPTY).isPresent();
            }
        });
    }

    private static String getRawFileName(String fileName) {
        return fileName.substring(fileName.lastIndexOf("/") + 1, fileName.lastIndexOf("."));
    }
}