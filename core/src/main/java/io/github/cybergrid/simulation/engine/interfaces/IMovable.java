package io.github.cybergrid.simulation.engine.interfaces;

import com.badlogic.gdx.math.Vector2;
import io.github.cybergrid.simulation.engine.movement.MovementComponent;


 // Interface for entities that can move through the world.
 // Provides both high-level movement API and physics component access.

public interface IMovable {

    // High-Level Movement API
    float getSpeed();

    void setSpeed(float speed);

    Vector2 getVelocity();

    void setVelocity(Vector2 velocity);


    // MovementManager Integration
    int getEntityId();

    MovementComponent getMovementComponent();

    Vector2 getPosition();

    void setPosition(Vector2 position);
}
