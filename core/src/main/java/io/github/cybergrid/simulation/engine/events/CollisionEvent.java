package io.github.cybergrid.simulation.engine.events;

import io.github.cybergrid.simulation.engine.entities.Entity;

public class CollisionEvent extends Event {
    private final Entity entityA;
    private final Entity entityB;

    public CollisionEvent(Entity a, Entity b) {
        this.entityA = a;
        this.entityB = b;
    }

    public Entity getEntityA() { return entityA; }
    public Entity getEntityB() { return entityB; }
}
