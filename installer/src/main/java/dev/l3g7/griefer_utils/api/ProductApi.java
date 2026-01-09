package dev.l3g7.griefer_utils.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.l3g7.griefer_utils.Constants;
import dev.l3g7.griefer_utils.api.ProductApi.LatestVersionResponse.Version;
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

    public String getDownloadLink(String scope) throws IOException {
        // Fetch latest version
	    LatestVersionResponse res = getJsonResponse(Constants.LATEST_VERSION_URL, LatestVersionResponse.class, objectMapper);
        Version version = scope.equalsIgnoreCase("beta") ? res.getBeta() : res.getStable();
        String guVersion = version.version;

        return switch (scope.toLowerCase()) {
            case "beta" -> String.format(Constants.BETA_DOWNLOAD_URL, guVersion);
            case "stable", "stabil" -> String.format(Constants.STABLE_DOWNLOAD_URL, guVersion, guVersion);
            default -> throw new IllegalStateException("Unexpected scope " + scope);
        };
    }

    public List<String> getAvailableScopes() {
        return List.of("Stabil", "Beta");
    }

    public boolean canInstallFor(GameInstance instance) {
        return instance.getMcVersion().equalsIgnoreCase("1.8.9") && instance.getLabyVersion() != -1;
    }

    public String getProductName() {
        return "GrieferUtils";
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @lombok.Data
    public static class LatestVersionResponse {
        public Version stable;
        public Version beta;

        @JsonIgnoreProperties(ignoreUnknown = true)
        @lombok.Data
        public static class Version {
            public String version;
        }
    }
}
