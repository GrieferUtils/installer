package dev.l3g7.griefer_utils.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class License {
    @JsonProperty("_id")
    private String id;
    private String key;
    private boolean active;
    private List<String> scope;
    private Owner owner;
    private UpdateStatus updateStatus;
    private Product product;
    private CreationData creationData;
    private List<Feature> features;
    private List<User> users;
    private boolean dodoguard;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Owner {
        @JsonProperty("_link")
        private String link;
        private String username;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class UpdateStatus {
        private boolean limited;
        private Instant validUntil;
        private List<AppliedCode> appliedCodes;

        @JsonIgnoreProperties(ignoreUnknown = true)
        @Data
        public static class AppliedCode {
            @JsonProperty("_internalId")
            private String internalId;
            private String code;
            private Instant usedAt;
            private int amount;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Product {
        private Identifier identifier;
        private List<Capability> capabilities;

        @JsonIgnoreProperties(ignoreUnknown = true)
        @Data
        public static class Identifier {
            private String key;
            private String translationKey;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        @Data
        public static class Capability {
            private String type;
            private int amount;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class CreationData {
        private Instant timestamp;
        private CreatedBy createdBy;

        @JsonIgnoreProperties(ignoreUnknown = true)
        @Data
        public static class CreatedBy {
            private String name;
            private String type;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Feature {
        @JsonProperty("_link")
        private String link;
        private Identifier identifier;
        private List<Product.Capability> capabilities;

        @JsonIgnoreProperties(ignoreUnknown = true)
        @Data
        public static class Identifier {
            private String key;
            private String translationKey;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class User {
        @JsonProperty("_internalId")
        private String internalId;
        private String username;
        private String uuid;
    }
}