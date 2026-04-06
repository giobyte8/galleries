package me.giobyte8.galleries.scanner.metadata.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A DTO for the JSON formatted metadata that exiftool retrieves
 * from media files.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExifToolMetadata {

    // This holds all wildcard "*Model*" matches
    private final Map<String, Object> otherTags = new HashMap<>();

    @JsonProperty("SourceFile")
    private String sourceFile;

    @JsonProperty("Make")
    @Getter(AccessLevel.NONE)
    private String cameraMaker;

    @JsonProperty("GPSLatitude")
    private Double latitude;

    @JsonProperty("GPSLongitude")
    private Double longitude;

    // Apple style
    @JsonProperty("CreationDate")
    private String appleDate;

    // Samsung/Standard style
    @JsonProperty("CreateDate")
    private String standardDate;

    @JsonAnySetter
    public void addOther(String key, Object value) {
        otherTags.put(key, value);
    }

    public Optional<String> resolvedCameraMaker() {
        if (Objects.nonNull(cameraMaker)) {
            return Optional.of(cameraMaker);
        }

        // If meta contains the key 'SamsungModel', then 'Samsung' is the maker
        var samsungKeyOpt = otherTags.keySet().stream()
                .map(String::toLowerCase)
                .filter(k -> k.contains("samsungmodel"))
                .findFirst();
        if (samsungKeyOpt.isPresent()) {
            return Optional.of("Samsung");
        }

        return Optional.empty();
    }

    /**
     * Pickup model from first tag that contains 'model' in its key.
     * This addresses the difference of style that Apple vs Samsung vs Other
     * devices use.
     * @return Camera model
     */
    public Optional<String> resolvedCameraModel() {
        // Logic to pick the best model name from the wildcard results
        return otherTags.entrySet().stream()
                .filter(e -> e.getKey().toLowerCase().contains("model"))
                .map(e -> e.getValue().toString())
                .findFirst();
    }
}
