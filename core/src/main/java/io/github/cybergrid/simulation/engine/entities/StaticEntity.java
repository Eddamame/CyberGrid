package io.github.cybergrid.simulation.engine.entities;

import com.badlogic.gdx.math.Vector2;
import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.collision.CollisionComponent;
import io.github.cybergrid.simulation.engine.interfaces.ICollidable;

public class StaticEntity extends Entity implements ICollidable {

    private Vector2 position;
    protected CollisionComponent collisionComponent;

    public StaticEntity(EngineContext context, int id, EntityType type) {
        super(context, id, type);
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
    public void onCollision(Entity other) {
    }

    @Override
    public Vector2 getPosition() {
        return position;
    }

    @Override
    public void setPosition(Vector2 pos) {
        position.set(pos);
    }

    @Override
    public void init() {
    }

    @Override
    public void update(float dt) {
    }

    @Override
    public void destroy() {
    }
}