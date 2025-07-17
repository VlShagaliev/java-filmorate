package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserEvent {
    @JsonProperty(value = "eventId")
    private Integer id;
    private Long timestamp;
    private Integer userId;
    private Integer entityId;
    private UserEventType eventType;
    private UserEventOperation operation;
}