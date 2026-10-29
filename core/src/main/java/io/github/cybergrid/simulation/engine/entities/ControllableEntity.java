package io.github.cybergrid.simulation.engine.entities;

import com.badlogic.gdx.math.Vector2;

import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.interfaces.IMovable;
import io.github.cybergrid.simulation.engine.movement.MovementComponent;
import io.github.cybergrid.simulation.engine.movement.SteeringState;
import io.github.cybergrid.simulation.engine.collision.CollisionComponent;
import io.github.cybergrid.simulation.engine.interfaces.ICollidable;

public class ControllableEntity extends Entity implements IMovable, ICollidable {

    private MovementComponent mc;
    
    protected CollisionComponent collisionComponent;

    public ControllableEntity(EngineContext context, int id, EntityType type) {
        super(context, id, type);
        this.mc = new MovementComponent() {
        };
        this.mc.setMaxSpeed(500f);
        this.mc.setMass(1.5f);
        this.mc.setSteeringBehavior(SteeringState.USER_CONTROLLED);
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
    public void init() {
        context.getMovementManager().registerEntity(entityID, mc);
    }

    @Override
    public void update(float dt) {}

    @Override
    public void destroy() {
        if (entityTexture != null) {
            entityTexture.dispose();
        }
        context.getMovementManager().unregisterEntity(entityID);
    }

    @Override
    public Vector2 getPosition() {
        return mc.getPosition();
    }

    @Override
    public void setPosition(Vector2 position) {
        mc.getPosition().set(position);
    }

    @Override
    public float getSpeed() {
        return mc.getMaxSpeed();
    }

    @Override
    public void setSpeed(float speed) {
        mc.setMaxSpeed(speed);
    }

    @Override
    public Vector2 getVelocity() {
        return mc.getVelocity();
    }

    @Override
    public void setVelocity(Vector2 velocity) {
        mc.getVelocity().set(velocity);
    }

    @Override
    public int getEntityId() {
        return entityID;
    }

    @Override
    public MovementComponent getMovementComponent() {
        return mc;
    }

}