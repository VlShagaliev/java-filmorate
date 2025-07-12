package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.UserEvent;
import ru.yandex.practicum.filmorate.model.UserEventOperation;
import ru.yandex.practicum.filmorate.model.UserEventType;

import java.util.Collection;

public interface UserEventStorage {

    void add(int userId, int entityId, UserEventType type, UserEventOperation operation);

    Collection<UserEvent> getUserEvents(int userId);
}