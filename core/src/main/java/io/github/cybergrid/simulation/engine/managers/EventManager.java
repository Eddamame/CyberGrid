package io.github.cybergrid.simulation.engine.managers;

import io.github.cybergrid.simulation.engine.events.Event;
import io.github.cybergrid.simulation.engine.events.EventBus;
import io.github.cybergrid.simulation.engine.interfaces.IEventListener;

public class EventManager extends EventBus<Event> {

    @Override
    protected <T extends Event> void onSubscribe(Class<T> type, IEventListener<T> listener) {
    }

    @Override
    protected <T extends Event> void onUnsubscribe(Class<T> type, IEventListener<T> listener) {
    }

    @Override
    protected void onDispatch(Object event, int listenerCount) {
    }
}
