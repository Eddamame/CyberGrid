package io.github.cybergrid.simulation.engine.entities;

import com.badlogic.gdx.math.Vector2;

import io.github.cybergrid.simulation.engine.EngineContext;
import io.github.cybergrid.simulation.engine.interfaces.IMovable;
import io.github.cybergrid.simulation.engine.movement.MovementComponent;
import io.github.cybergrid.simulation.engine.movement.SteeringState;
import io.github.cybergrid.simulation.engine.collision.CollisionComponent;
import io.github.cybergrid.simulation.engine.interfaces.ICollidable;

public class AIEntity extends Entity implements IMovable, ICollidable {

    private MovementComponent mc;
    private float recoveryTime = 0f;
    private int targetEntityId = -1;
    
    protected CollisionComponent collisionComponent;

    public AIEntity(EngineContext context, int id, EntityType type) {
        super(context, id, type);
        this.mc = new MovementComponent();
        this.mc.setMaxSpeed(900f);
        this.mc.setMass(1.0f);
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

    public void setTargetEntity(int id) {
        this.targetEntityId = id;
    }

    @Override
    public void init() {
        context.getMovementManager().registerEntity(entityID, mc);
    }

    @Override
    public void update(float dt) {

        if (recoveryTime > 0) {
            recoveryTime -= dt;
            mc.setSteeringBehavior(SteeringState.IDLE);
        } else {
            Entity player = context.getEntityManager().getEntity(targetEntityId);
            if (player != null) {
                mc.getTargetPosition().set(player.getPosition());
                mc.setSteeringBehavior(SteeringState.SEEK);
            }
        }
    }


    @Override
    public void destroy() {
        if (entityTexture != null) {
            entityTexture.dispose();
        }
        context.getMovementManager().unregisterEntity(entityID);
    }

    public void onCollisionBounce() {
        this.recoveryTime = 0.6f;
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

    public void setTarget(Vector2 target) {
        if (target != null) {
            mc.getTargetPosition().set(target);
            mc.setSteeringBehavior(SteeringState.SEEK);
        }
    }

    public Vector2 getTarget() {
        return mc.getTargetPosition().cpy();
    }

    public boolean hasReachedTarget(float threshold) {
        return mc.getPosition().dst(mc.getTargetPosition()) <= threshold;
    }
}