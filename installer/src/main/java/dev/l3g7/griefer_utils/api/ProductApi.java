package dev.l3g7.griefer_utils.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.l3g7.griefer_utils.Constants;
import dev.l3g7.griefer_utils.data.GameInstance;

import java.io.IOException;
import java.util.List;

import static dev.l3g7.griefer_utils.Util.getJsonResponse;

/**
 * API responsible for download links.
 */
public class ProductApi {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    private static ProductApi INSTANCE;

    public static ProductApi getInstance() {
        if (INSTANCE == null) INSTANCE = new ProductApi();
        return INSTANCE;
    }

    public DownloadResponse getDownloadLink(GameInstance instance, boolean beta) throws IOException {
        String version = instance.getMcVersion();
        String[] version_parts = version.split("\\.");
        String guVersion = version_parts[0] + version_parts[1];
        String reqUrl = String.format(Constants.STABLE_DOWNLOAD_URL, guVersion, guVersion);
        return getJsonResponse(reqUrl, DownloadResponse.class, objectMapper);
    }


    public List<String> getAvailableScopes() {
        return List.of("Stabil", "Beta");
    }

    public boolean canInstallFor(GameInstance instance, String scope) {
        try {
            return getDownloadLink(instance, true).success; // TODO beta: true
        } catch (Exception e) {
            return false;
        }
    }

    public String getProductName() {
        return "GrieferUtils";
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @lombok.Data
    public static class DownloadResponse {
        public boolean success;

        @com.fasterxml.jackson.annotation.JsonProperty()
        public String link = "no-link-provided";
    }
}
