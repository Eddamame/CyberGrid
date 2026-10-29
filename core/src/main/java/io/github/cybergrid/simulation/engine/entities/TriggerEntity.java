package io.github.cybergrid.simulation.engine.entities;

import com.badlogic.gdx.math.Vector2;
import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.interfaces.TriggerCallback;
import io.github.cybergrid.simulation.engine.collision.CollisionComponent;
import io.github.cybergrid.simulation.engine.interfaces.ICollidable;

public class TriggerEntity extends Entity implements ICollidable {
    private TriggerCallback callback;
    private Vector2 position;
    
    protected CollisionComponent collisionComponent;
    
    public TriggerEntity(EngineContext context, int entityID, EntityType entityType) {
        super(context, entityID, entityType);
        this.position = new Vector2();
    }
    
    @Override
    public CollisionComponent getCollisionComponent() {
        return collisionComponent;
    }

    public void setCollisionComponent(CollisionComponent collisionComponent) {
        this.collisionComponent = collisionComponent;
    }

    @Override
    public void onCollision(Entity other) {}

    @Override 
    public Vector2 getPosition() { return position; }

    @Override 
    public void setPosition(Vector2 pos) { position.set(pos); }

    @Override
    public void init() {
    }
    
    @Override
    public void update(float deltaTime) {
    }

    @Override
    public void destroy() {
    }
    
    public void activate(Entity activator) {
        if (callback != null) {
            callback.onTrigger(this, activator);
        }
    }
    
    public void activate() {
        activate(null);
    }
    
    public void setCallback(TriggerCallback callback) {
        this.callback = callback;
    }
}