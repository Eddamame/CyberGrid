package io.github.cybergrid.simulation.engine.managers;

import com.badlogic.gdx.math.Vector2;
import io.github.cybergrid.simulation.engine.movement.MovementComponent;
import io.github.cybergrid.simulation.engine.movement.SteeringState;
import io.github.cybergrid.simulation.engine.interfaces.IMovable;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class MovementManager {

    // Core registry - stores all movement components
    private Map<Integer, MovementComponent> registry;

    private static final float WAYPOINT_THRESHOLD = 20f;
    private static final float IMPULSE_SPEED_THRESHOLD = 1000f;
    private static final float FRICTION_VELOCITY_HEADROOM = 1.5f;
    private static final int   TARGET_FRAME_RATE = 60;
    private static final float DRIFT_FORCE = 50f;

    public MovementManager() {
        this.registry = new HashMap<>();
    }

    // Register entity with component
    public void registerEntity(int entityId, MovementComponent initialData) {
        registry.put(entityId, initialData);
    }

    // Register via IMovable interface
    public void registerMovable(IMovable entity) {
        int id = entity.getEntityId();
        MovementComponent mc = entity.getMovementComponent();
        registry.put(id, mc);
    }

    // Unregister entity
    public void unregisterEntity(int entityId) {
        registry.remove(entityId);
    }

    // Update entity position from component
    public void syncEntityPosition(int entityId, Vector2 entityPosition) {
        MovementComponent mc = registry.get(entityId);
        if (mc != null) {
            entityPosition.set(mc.getPosition());
        }
    }

    // apply impulse force to entity - used by CollisionManager
    public void applyImpulse(int entityId, Vector2 impulseVelocity) {
        MovementComponent mc = registry.get(entityId);
        if (mc == null) return;
        mc.getVelocity().set(impulseVelocity);
    }

    // position displacement
    public void displacePosition(int entityId, Vector2 displacement) {
        MovementComponent mc = registry.get(entityId);
        if (mc == null) return;

        mc.getPosition().add(displacement);
    }


    // Main update - called every frame
    public void update(float deltaTime, float worldWidth, float worldHeight) {
        for (Map.Entry<Integer, MovementComponent> entry : registry.entrySet()) {
            MovementComponent mc = entry.getValue();

            // Apply steering behavior forces
            applySteering(mc);

            // Integrate physics
            integrate(mc, deltaTime, worldWidth, worldHeight);
        }
    }

    private void integrate(MovementComponent mc, float deltaTime, float worldWidth, float worldHeight) {
        // velocity += acceleration * deltaTime
        mc.getVelocity().add(mc.getAcceleration().cpy().scl(deltaTime));

        // Frame-rate independent friction - skip if high impulse state
        if (mc.getMaxSpeed() < IMPULSE_SPEED_THRESHOLD && mc.getVelocity().len() < mc.getMaxSpeed() * FRICTION_VELOCITY_HEADROOM) {
            mc.getVelocity().scl((float) Math.pow(mc.getFrictionCoefficient(), deltaTime * TARGET_FRAME_RATE));
        }

        // Clamp to maxSpeed
        if (mc.getVelocity().len() > mc.getMaxSpeed()) {
            mc.getVelocity().setLength(mc.getMaxSpeed());
        }

        // position += velocity * deltaTime
        mc.getPosition().add(mc.getVelocity().cpy().scl(deltaTime));

        // Wall clamping/bouncing
        clampToWorld(mc, worldWidth, worldHeight);

        // Reset acceleration each frame
        mc.getAcceleration().set(0, 0);
    }

    // Wall boundary
    private void clampToWorld(MovementComponent mc, float worldWidth, float worldHeight) {
        float margin = mc.getWorldMargin();

        if (mc.getPosition().x < margin) {
            mc.getPosition().x = margin;
            mc.getVelocity().x = 0;
        } else if (mc.getPosition().x > worldWidth - margin) {
            mc.getPosition().x = worldWidth - margin;
            mc.getVelocity().x = 0;
        }

        if (mc.getPosition().y < margin) {
            mc.getPosition().y = margin;
            mc.getVelocity().y = 0;
        } else if (mc.getPosition().y > worldHeight - margin) {
            mc.getPosition().y = worldHeight - margin;
            mc.getVelocity().y = 0;
        }
    }

    // Apply force to entity - routes through acceleration pipeline
    public void applyForce(int entityId, Vector2 force) {
        MovementComponent mc = registry.get(entityId);
        if (mc == null) return;

        // F = ma -> a = F/m
        Vector2 acceleration = force.cpy().scl(1f / mc.getMass());
        mc.getAcceleration().add(acceleration);
    }

    // Set steering behavior
    public void setSteeringBehavior(int entityId, SteeringState behavior) {
        MovementComponent mc = registry.get(entityId);
        if (mc != null) {
            mc.setSteeringBehavior(behavior);
        }
    }

    // Set target for steering
    public void setTargetPosition(int entityId, Vector2 target) {
        MovementComponent mc = registry.get(entityId);
        if (mc != null) {
            mc.getTargetPosition().set(target);
        }
    }

    // Apply steering forces
    private void applySteering(MovementComponent mc) {
        if (mc.getSteeringBehavior() == null || mc.getSteeringBehavior() == SteeringState.IDLE) {
            return;
        }

        Vector2 steeringForce = calculateSteeringForce(mc);
        mc.getAcceleration().add(steeringForce.scl(1f / mc.getMass()));
    }

    private Vector2 calculateSteeringForce(MovementComponent mc) {
        switch (mc.getSteeringBehavior()) {
            case SEEK:
                return seek(mc);
            case EVADE:
                return evade(mc);
            case DRIFT:
                return drift(mc);
            case FOLLOW_PATH:
                return followPath(mc);
            default:
                return new Vector2();
        }
    }

    private Vector2 seek(MovementComponent mc) {
        Vector2 desired = mc.getTargetPosition().cpy().sub(mc.getPosition()).nor().scl(mc.getMaxSpeed());
        return desired.sub(mc.getVelocity());
    }

    private Vector2 evade(MovementComponent mc) {
        Vector2 desired = mc.getPosition().cpy().sub(mc.getTargetPosition()).nor().scl(mc.getMaxSpeed());
        return desired.sub(mc.getVelocity());
    }

    private Vector2 drift(MovementComponent mc) {
        float angle = (float) (Math.random() * Math.PI * 2);
        return new Vector2((float) Math.cos(angle), (float) Math.sin(angle)).scl(DRIFT_FORCE);
    }

    private Vector2 followPath(MovementComponent mc) {
        List<Vector2> waypoints = mc.getWaypoints();
        if (waypoints == null || waypoints.isEmpty()) return new Vector2();

        int idx = mc.getCurrentWaypointIndex();
        if (idx >= waypoints.size()) {
            mc.setSteeringBehavior(SteeringState.IDLE);
            return new Vector2();
        }

        Vector2 target = waypoints.get(idx);
        if (mc.getPosition().dst(target) < WAYPOINT_THRESHOLD) {
            mc.setCurrentWaypointIndex(idx + 1);
            if (idx + 1 >= waypoints.size()) {
                mc.setSteeringBehavior(SteeringState.IDLE);
                return new Vector2();
            }
            target = waypoints.get(idx + 1);
        }

        Vector2 desired = target.cpy().sub(mc.getPosition()).nor().scl(mc.getMaxSpeed());
        return desired.sub(mc.getVelocity());
    }

    // Set waypoints for FOLLOW_PATH behavior
    public void setWaypoints(int entityId, List<Vector2> waypoints) {
        MovementComponent mc = registry.get(entityId);
        if (mc != null) mc.setWaypoints(waypoints);
    }

    // Setter for direct position setting
    public void setPosition(int entityId, Vector2 position) {
        MovementComponent mc = registry.get(entityId);
        if (mc != null) mc.getPosition().set(position);
    }

    // Getter
    public MovementComponent getComponent(int entityId) {
        return registry.get(entityId);
    }
}
