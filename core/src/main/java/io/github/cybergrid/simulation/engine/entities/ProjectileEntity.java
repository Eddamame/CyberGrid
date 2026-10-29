package io.github.cybergrid.simulation.engine.entities;

import com.badlogic.gdx.math.Vector2;
import io.github.cybergrid.simulation.engine.interfaces.IMovable;
import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.movement.MovementComponent;
import io.github.cybergrid.simulation.engine.movement.SteeringState;
import io.github.cybergrid.simulation.engine.collision.CollisionComponent;
import io.github.cybergrid.simulation.engine.interfaces.ICollidable;

public class ProjectileEntity extends Entity implements IMovable, ICollidable {

    private MovementComponent mc;

    protected CollisionComponent collisionComponent;

    public ProjectileEntity(EngineContext context, int id, EntityType type) {
        super(context, id, type);
        this.mc = new MovementComponent() {};
        this.mc.setMaxSpeed(1000f);
        this.mc.setMass(0.5f);
        this.mc.setSteeringBehavior(SteeringState.IDLE);
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
    public void update(float dt) {
    }

    @Override
    public void destroy() {
        if (entityTexture != null) {
            entityTexture.dispose();
        }
        context.getMovementManager().unregisterEntity(entityID);
    }

    public void launch(Vector2 direction, float speed) {
        mc.getVelocity().set(direction).nor().scl(speed);
        mc.setSteeringBehavior(SteeringState.FOLLOW_PATH);
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
        if (mc.getVelocity().len() > 0) {
            mc.getVelocity().setLength(speed);
        }
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