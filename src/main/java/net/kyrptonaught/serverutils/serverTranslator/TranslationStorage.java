package net.kyrptonaught.serverutils.serverTranslator;

import java.util.HashMap;

public class TranslationStorage {
    public static final String EN_US = "en_us";

    private static final HashMap<String, LanguageStorage> translations = new HashMap<>();

    public static String getTranslationFor(String lang, String key) {
        if (!translations.containsKey(lang)) lang = EN_US;

        String translation = translations.get(lang).getTranslation(key);
        if (translation == null) translation = translations.get(EN_US).getTranslation(key);
        if (translation == null) translation = key;
        return translation;
    }

    public static void addTranslation(String lang, String key, String value) {
        if (!translations.containsKey(lang))
            translations.put(lang, new LanguageStorage());

        translations.get(lang).addTranslation(key, value);
    }

    public static void clear() {
        translations.clear();
    }

    public static HashMap<String, String> getAllTranslations(String lang) {
        return translations.get(lang).translations;
    }

    public static class LanguageStorage {
        private final HashMap<String, String> translations = new HashMap<>();

        public String getTranslation(String key) {
            return translations.get(key);
        }

        public void addTranslation(String key, String value) {
            translations.put(key, value);
        }
    }
}
