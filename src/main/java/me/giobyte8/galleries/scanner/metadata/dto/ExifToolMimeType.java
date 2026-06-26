package me.giobyte8.galleries.scanner.metadata.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExifToolMimeType {

    @JsonProperty("SourceFile")
    private String sourceFile;

    @JsonProperty("MIMEType")
    private String mimeType;
}
