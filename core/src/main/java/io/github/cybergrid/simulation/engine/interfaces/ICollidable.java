package io.github.cybergrid.simulation.engine.interfaces;

import com.badlogic.gdx.math.Vector2;
import io.github.cybergrid.simulation.engine.collision.CollisionComponent;
import io.github.cybergrid.simulation.engine.entities.Entity;

public interface ICollidable {
    CollisionComponent getCollisionComponent();
    Vector2 getPosition();
    int getID();
    void onCollision(Entity other);
    default void onCollisionExit(Entity other) {}
}