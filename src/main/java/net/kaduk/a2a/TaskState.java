// src/main/java/net/kaduk/a2a/TaskState.java
package net.kaduk.a2a;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskState {
    SUBMITTED("submitted"),
    WORKING("working"),
    INPUT_REQUIRED("input-required"),
    COMPLETED("completed"),
    CANCELED("canceled"),
    FAILED("failed"),
    REJECTED("rejected"),
    AUTH_REQUIRED("auth-required"),
    UNKNOWN("unknown");

    private final String value;

    TaskState(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TaskState fromValue(String value) {
        for (TaskState state : TaskState.values()) {
            if (state.value.equals(value)) {
                return state;
            }
        }
        return UNKNOWN;
    }
}