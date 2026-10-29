package io.github.cybergrid.simulation.engine.movement;

import com.badlogic.gdx.math.Vector2;
import java.util.List;
import java.util.ArrayList;

public class MovementComponent {

    private final Vector2 position;
    private final Vector2 velocity;
    private final Vector2 acceleration;
    private final Vector2 targetPosition;
    private float maxSpeed;
    private float mass;
    private float frictionCoefficient = 0.95f;
    private float worldMargin = 45f;
    private SteeringState steeringBehavior;
    private List<Vector2> waypoints = new ArrayList<>();
    private int currentWaypointIndex = 0;

    // Default constructor - starts at origin (0, 0)
    public MovementComponent() {
        this.position = new Vector2();
        this.velocity = new Vector2();
        this.acceleration = new Vector2();
        this.targetPosition = new Vector2();
        this.maxSpeed = 400f;
        this.mass = 1.0f;
        this.steeringBehavior = SteeringState.IDLE;
    }

    // Convenience constructor - starts at given position
    public MovementComponent(Vector2 startPos) {
        this.position = new Vector2(startPos);
        this.velocity = new Vector2();
        this.acceleration = new Vector2();
        this.targetPosition = new Vector2();
        this.maxSpeed = 400f;
        this.mass = 1.0f;
        this.steeringBehavior = SteeringState.IDLE;
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public Vector2 getAcceleration() {
        return acceleration;
    }

    public Vector2 getTargetPosition() {
        return targetPosition;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public float getMass() {
        return mass;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public float getFrictionCoefficient() {
        return frictionCoefficient;
    }

    public void setFrictionCoefficient(float frictionCoefficient) {
        this.frictionCoefficient = frictionCoefficient;
    }

    public float getWorldMargin() {
        return worldMargin;
    }

    public void setWorldMargin(float worldMargin) {
        this.worldMargin = worldMargin;
    }

    public SteeringState getSteeringBehavior() {
        return steeringBehavior;
    }

    public void setSteeringBehavior(SteeringState steeringBehavior) {
        this.steeringBehavior = steeringBehavior;
    }

    public List<Vector2> getWaypoints() { return waypoints; }

    public void setWaypoints(List<Vector2> waypoints) {
        this.waypoints = waypoints;
        this.currentWaypointIndex = 0;
    }

    public int getCurrentWaypointIndex() { return currentWaypointIndex; }

    public void setCurrentWaypointIndex(int index) { this.currentWaypointIndex = index; }

}
