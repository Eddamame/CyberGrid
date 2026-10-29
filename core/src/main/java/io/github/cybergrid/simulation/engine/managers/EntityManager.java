package io.github.cybergrid.simulation.engine.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;


import io.github.cybergrid.simulation.engine.entities.Entity;
import io.github.cybergrid.simulation.engine.entities.EntityType;
import io.github.cybergrid.simulation.engine.events.EntityDestroyEvent;
import io.github.cybergrid.simulation.engine.events.EntitySpawnEvent;

public class EntityManager {

    private final EventManager eventManager;
    private Map<Integer, Entity> entities = new HashMap<>();
    private int nextID = 1;

    // Factory registry for creating entities by type
    private Map<EntityType, BiFunction<Integer, EntityType, Entity>> factories = new HashMap<>();


    public EntityManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    public void registerFactory(EntityType type, BiFunction<Integer, EntityType, Entity> factory) {
        factories.put(type, factory);
    }

    // Factory method - using registry to create entities by type
    public int createEntity(EntityType type) {
        int id = nextID++;

        // Look up the factory for this type
        BiFunction<Integer, EntityType, Entity> factory = factories.get(type);
        if (factory == null) {
            throw new IllegalArgumentException("Unknown entity type: " + type);
        }

        // Use the factory to create the entity
        Entity e = factory.apply(id, type);
        e.init();
        entities.put(id, e);

        // Dispatch EntitySpawnEvent.
        eventManager.dispatch(new EntitySpawnEvent(e, e.getPosition()));

        return id;
    }

    // Properly destroys and removes entity
    public void destroyEntity(int entityID) {
        Entity entity = entities.get(entityID);
        if (entity != null) {
            entity.destroy();
            entities.remove(entityID);

            // Dispatch EntityDestroyEvent only when an entity actually existed.
            eventManager.dispatch(new EntityDestroyEvent(entity));
        }
    }

    // Safe update - copy list to avoid ConcurrentModificationException
    public void update(float dt) {
        List<Entity> toUpdate = new ArrayList<>(entities.values());
        for (Entity e : toUpdate) {
            e.update(dt);
        }
    }

    // Properly calls destroy() on all entities before clearing
    public void cleanup() {
        for (Entity e : entities.values()) {
            e.destroy();
        }
        entities.clear();
        nextID = 1;
    }

    // ========================================
    // Query Methods
    // ========================================
    public Entity getEntity(int id) {
        return entities.get(id);
    }

    public <T extends Entity> T getEntityAs(int id, Class<T> type) {
        Entity entity = entities.get(id);
        if (entity == null || !type.isInstance(entity)) {
            return null;
        }
        return type.cast(entity);
    }

    public List<Entity> getAllEntities() {
        return new ArrayList<>(entities.values());
    }

    // Filter entities by type string
    public List<Entity> getEntitiesByType(EntityType type) {
        return entities.values().stream()
                .filter(e -> e.getType() == type)
                .collect(Collectors.toList());
    }

}
