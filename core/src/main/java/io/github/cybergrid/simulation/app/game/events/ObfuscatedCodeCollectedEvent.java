package io.github.cybergrid.simulation.app.game.events;

import io.github.cybergrid.simulation.engine.events.Event;

public class ObfuscatedCodeCollectedEvent extends Event {
    private final int collectorEntityId;

    public ObfuscatedCodeCollectedEvent(int collectorEntityId) {
        this.collectorEntityId = collectorEntityId;
    }

    public int getCollectorEntityId() {
        return collectorEntityId;
    }
}
