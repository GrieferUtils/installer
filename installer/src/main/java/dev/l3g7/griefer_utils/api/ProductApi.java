package dev.l3g7.griefer_utils.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.l3g7.griefer_utils.Constants;
import dev.l3g7.griefer_utils.data.GameInstance;
import dev.l3g7.griefer_utils.data.License;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static dev.l3g7.griefer_utils.Util.getJsonResponse;

/**
 * API responsible for download links.
 */
public class ProductApi {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private static ProductApi INSTANCE;
    HashMap<String, DownloadResponse> downloadCache = new HashMap<>();
    private License license = null;

    public static ProductApi getInstance() {
        if (INSTANCE == null) INSTANCE = new ProductApi();
        return INSTANCE;
    }

    public DownloadResponse getDownloadLink(GameInstance instance, String scope) throws IOException {
        String key = license.getKey() + scope + instance.getMcVersion();
        if (downloadCache.containsKey(key)) return downloadCache.get(key);
        scope = scope.toLowerCase();
        String version = instance.getMcVersion();
        String[] version_parts = version.split("\\.");
        String guVersion = version_parts[0] + version_parts[1];
        String reqUrl = String.format(Constants.PRODUCT_DOWNLOAD_URL, license.getKey(), license.getProduct().getIdentifier().getKey(), guVersion, scope);
        DownloadResponse res = getJsonResponse(reqUrl, DownloadResponse.class, objectMapper);
        downloadCache.put(key, res);
        return res;
    }


    public List<String> getAvailableScopes() {
        return this.license.getScope();
    }

    public boolean canInstallFor(GameInstance instance, String scope) {
        try {
            return getDownloadLink(instance, scope).success;
        } catch (Exception e) {
            return false;
        }
    }

    public String getLicenseDownloadUrl() {
        if (this.license == null) return null;
        return String.format(Constants.LICENSE_DOWNLOAD_URL, this.license.getKey(), this.license.getOwner().getLink());
    }

    public String getProductName() {
        if (this.license == null) return null;
        return TranslationApi.getInstance().get(license.getProduct().getIdentifier().getTranslationKey());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @lombok.Data
    public static class DownloadResponse {
        public boolean success;

        @com.fasterxml.jackson.annotation.JsonProperty()
        public String link = "no-link-provided";
    }
}
