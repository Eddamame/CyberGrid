package io.github.cybergrid.simulation.engine.events;

import com.badlogic.gdx.math.Vector2;

import io.github.cybergrid.simulation.engine.entities.Entity;

public class EntitySpawnEvent extends Event {
    private Entity entity;
    private Vector2 position;

    public EntitySpawnEvent(Entity entity, Vector2 position) {
        this.entity = entity;
        this.position = position;
    }

    public Entity getEntity() { return entity; }
    public Vector2 getPosition() { return position; }
}
