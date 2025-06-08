package com.example.a2a;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilePart extends Part {
    @Builder.Default
    @JsonProperty("kind")
    private final String kind = "file";

    @JsonProperty("file")
    private Object file; // could be FileWithBytes or FileWithUri

    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}

// FileWithBytes.java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
class FileWithBytes {
    @JsonProperty("bytes")
    private String bytes; // base64

    @JsonProperty("mimeType")
    private String mimeType;

    @JsonProperty("name")
    private String name;
}

// FileWithUri.java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
class FileWithUri {
    @JsonProperty("uri")
    private String uri;

    @JsonProperty("mimeType")
    private String mimeType;

    @JsonProperty("name")
    private String name;
}
