package io.github.cybergrid.simulation.engine.events;

import io.github.cybergrid.simulation.engine.entities.Entity;

public class EntityDestroyEvent extends Event {
    private Entity entity;

    public EntityDestroyEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() { return entity; }
}
