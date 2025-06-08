package com.example.a2a;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "kind",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextPart.class, name = "text"),
        @JsonSubTypes.Type(value = FilePart.class, name = "file"),
        @JsonSubTypes.Type(value = DataPart.class, name = "data")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public abstract class Part {
    private String kind;
}
