package net.kyrptonaught.serverutils.serverTranslator;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.kyrptonaught.serverutils.ModuleWConfig;
import net.kyrptonaught.serverutils.ServerUtilsMod;
import net.kyrptonaught.serverutils.mixin.serverTranslator.ServerPlayerEntityLanguageAccessor;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import net.minecraft.util.Language;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Optional;

public class ServerTranslator extends ModuleWConfig<ServerTranslationConfig> {

    public static String getLanguage(ServerPlayerEntity player) {
        if (player == null) return TranslationStorage.EN_US;
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

    public static Text translate(Text text) {
        MutableText output = Text.empty();
        if (text.getContent() instanceof TranslatableTextContent trans) {
            String out = translate(trans.getKey());
            if (out.equals(trans.getKey()) && trans.getFallback() != null) out = trans.getFallback();
            for (Object arg : trans.getArgs()) {
                if (arg instanceof MutableText argText) {

                    StringBuilder sb = new StringBuilder();
                    if (argText.getStyle().getColor() != null)
                        sb.append(Formatting.byName(argText.getStyle().getColor().getName().toUpperCase()));
                    if (argText.getStyle().isBold()) sb.append(Formatting.BOLD);
                    if (argText.getStyle().isItalic()) sb.append(Formatting.ITALIC);
                    if (argText.getStyle().isUnderlined()) sb.append(Formatting.UNDERLINE);
                    if (argText.getStyle().isStrikethrough()) sb.append(Formatting.STRIKETHROUGH);
                    if (argText.getStyle().isObfuscated()) sb.append(Formatting.OBFUSCATED);
                    sb.append(translate(argText).getString());
                    sb.append(Formatting.RESET);

                    out = out.replace("%s", sb.toString());
                } else out = out.replace("%s", arg.toString());
            }
            output.append(Text.translatable(out).setStyle(text.getStyle()));
        } else output.append(MutableText.of(text.getContent()).setStyle(text.getStyle()));

        for (Text sibling : text.getSiblings()) output.append(translate(sibling));

        return output;
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
