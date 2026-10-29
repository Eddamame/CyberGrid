package io.github.cybergrid.simulation.engine.events;

import io.github.cybergrid.simulation.engine.interfaces.IEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class EventBus<E> {
    private final Map<Class<? extends E>, List<IEventListener<? extends E>>> listeners = new HashMap<>();

    public <T extends E> void subscribe(Class<T> type, IEventListener<T> listener) {
        listeners.computeIfAbsent(type, k -> new ArrayList<>()).add(listener);
        onSubscribe(type, listener);
    }

    public <T extends E> void unsubscribe(Class<T> type, IEventListener<T> listener) {
        List<IEventListener<? extends E>> users = listeners.get(type);
        if (users != null) {
            users.remove(listener);
            if (users.isEmpty()) listeners.remove(type);
            onUnsubscribe(type, listener);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends E> void dispatch(T event) {
        List<IEventListener<? extends E>> users = listeners.get(event.getClass());
        onDispatch(event, users != null ? users.size() : 0);
        if (users != null) {
            new ArrayList<>(users).forEach(l -> ((IEventListener<T>) l).onEvent(event));
        }
    }

    protected <T extends E> void onSubscribe(Class<T> type, IEventListener<T> listener) { }
    protected <T extends E> void onUnsubscribe(Class<T> type, IEventListener<T> listener) { }
    protected <T extends E> void onDispatch(Object event, int listenerCount) { }
}
