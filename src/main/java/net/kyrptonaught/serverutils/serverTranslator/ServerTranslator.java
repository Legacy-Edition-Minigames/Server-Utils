package net.kyrptonaught.serverutils.serverTranslator;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.kyrptonaught.serverutils.ModuleWConfig;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.mixin.serverTranslator.ServerPlayerEntityLanguageAccessor;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.TextVisitFactory;
import net.minecraft.util.Language;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Optional;

public class ServerTranslator extends ModuleWConfig<ServerTranslationConfig> {

    public static String getLanguage(ServerPlayerEntity player) {
        return ((ServerPlayerEntityLanguageAccessor) player).getLanguage();
    }

    public static String translate(ServerPlayerEntity player, String key) {
        return TranslationStorage.getTranslationFor(getLanguage(player), key);
    }

    public static String translate(String key) {
        return translate(null, key);
    }

    @Override
    public void onInitialize() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new TranslationLoader());
    }

    public static void injectTranslations() {
        HashMap<String, String> builder = new HashMap<>();

        try (InputStream inputStream = Language.class.getResourceAsStream("/assets/minecraft/lang/en_us.json")) {
            Language.load(inputStream, builder::put);
        } catch (Exception e) {
            e.printStackTrace();
        }

        HashMap<String, String> loadedTranslations = TranslationStorage.getAllTranslations(TranslationStorage.EN_US);
        if (loadedTranslations != null) {
            builder.putAll(TranslationStorage.getAllTranslations(TranslationStorage.EN_US));
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

    @Override
    public ServerTranslationConfig createDefaultConfig() {
        return new ServerTranslationConfig();
    }
}
