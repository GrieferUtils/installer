package de.byteandbit.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.byteandbit.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class TranslationApi {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static TranslationApi instance;
    private final Map<String, String> translations = new HashMap<>();

    public static TranslationApi getInstance() {
        if (instance == null) instance = new TranslationApi();
        return instance;
    }

    public void loadLocalLanguage(String languageCode) {
        String json_file = String.format(Constants.i18nPathLocal, languageCode);
        try (InputStream is = getClass().getResource(json_file).openStream()) {
            translations.putAll(mapper.readValue(is, new TypeReference<Map<String, String>>() {
            }));
        } catch (Exception ignored) {
            ignored.printStackTrace();
            loadLocalLanguage("en");
        }
    }

    public void loadRemoteLanguage(String languageCode) throws IOException {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(String.format(Constants.i18nPathRemote, languageCode)).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            try (InputStream is = connection.getInputStream()) {
                Map<String, String> remoteTranslations = mapper.readValue(is, new TypeReference<Map<String, String>>() {
                });
                translations.putAll(remoteTranslations);
            }
            if (connection.getResponseCode() == 404) {
                loadRemoteLanguage("en");
            }
        } catch (IOException e) {
            throw new IOException(e);
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    public String get(String key) {
        if (!translations.containsKey(key)) System.out.println("Missing translation for key: " + key);
        return translations.getOrDefault(key, key);
    }


}
