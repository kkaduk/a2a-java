package net.kaduk.a2a;

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
public class TextPart extends Part {
    @Builder.Default
    @JsonProperty("kind")
    private final String kind = "text";

    @JsonProperty("text")
    private String text;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;
}
