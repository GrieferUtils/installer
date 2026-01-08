package de.byteandbit.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.byteandbit.Constants;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * API for handling translations, providing local and remote language loading.
 */
public class TranslationApi {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static TranslationApi instance;
    private final Map<String, String> translations = new HashMap<>();

    public static TranslationApi getInstance() {
        if (instance == null) instance = new TranslationApi();
        return instance;
    }

    public void loadLocalLanguage() {
        try (InputStream is = getClass().getResource(Constants.I18N_PATH).openStream()) {
            translations.putAll(mapper.readValue(is, new TypeReference<Map<String, String>>() {
            }));
        } catch (Exception e) {
            throw new IllegalStateException("Could not load i18n file!", e);
        }
    }

    public String get(String key) {
        if (!translations.containsKey(key))
            throw new IllegalStateException("Missing translation for key: " + key);

        return translations.getOrDefault(key, key);
    }


}
