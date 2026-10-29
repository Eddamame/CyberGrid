package io.github.cybergrid.simulation.engine.interfaces;

@FunctionalInterface
public interface IEventListener<T> {
    void onEvent(T event);
}
