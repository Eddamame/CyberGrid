package io.github.cybergrid.simulation.engine;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.cybergrid.simulation.engine.collision.CollisionDetector;
import io.github.cybergrid.simulation.engine.collision.CollisionHandler;
import io.github.cybergrid.simulation.engine.collision.SpatialHashGrid;
import io.github.cybergrid.simulation.engine.managers.AudioManager;
import io.github.cybergrid.simulation.engine.managers.CollisionManager;
import io.github.cybergrid.simulation.engine.managers.EntityManager;
import io.github.cybergrid.simulation.engine.managers.EventManager;
import io.github.cybergrid.simulation.engine.managers.MovementManager;
import io.github.cybergrid.simulation.engine.managers.SceneManager;

public final class EngineContext {

    private final EventManager eventManager;
    private final MovementManager movementManager;
    private final EntityManager entityManager;
    private final CollisionManager collisionManager;
    private final AudioManager audioManager;
    private final SceneManager sceneManager;
    private final Map<Class<?>, Object> services;
    private SpriteBatch sharedBatch;
    private BitmapFont sharedFont;

    public EngineContext() {
            this.eventManager = new EventManager();
            this.movementManager = new MovementManager();
            this.entityManager = new EntityManager(eventManager);
            
            CollisionDetector detector = new CollisionDetector();
            CollisionHandler handler = new CollisionHandler(this.movementManager);
            SpatialHashGrid grid = new SpatialHashGrid(128);
            this.collisionManager = new CollisionManager(this.entityManager, this.eventManager, detector, handler, grid);
            
            this.audioManager = new AudioManager();
            this.sceneManager = new SceneManager();
            this.services = new HashMap<>();
        }

    public EventManager getEventManager() {
        return eventManager;
    }

    public MovementManager getMovementManager() {
        return movementManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public CollisionManager getCollisionManager() {
        return collisionManager;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public SceneManager getSceneManager() {
        return sceneManager;
    }

    public void setSharedRenderResources(SpriteBatch batch, BitmapFont font) {
        this.sharedBatch = batch;
        this.sharedFont = font;
    }

    public SpriteBatch getSharedBatch() {
        return sharedBatch;
    }

    public BitmapFont getSharedFont() {
        return sharedFont;
    }

    public <T> void registerService(Class<T> type, T service) {
        if (type == null || service == null) {
            return;
        }
        services.put(type, service);
    }

    public <T> T getService(Class<T> type) {
        if (type == null) {
            return null;
        }
        Object service = services.get(type);
        if (service == null) {
            return null;
        }
        return type.cast(service);
    }
}
