package io.github.cybergrid.simulation.engine.collision;

import com.badlogic.gdx.math.Rectangle;
import io.github.cybergrid.simulation.engine.interfaces.ICollidable;

public class CollisionDetector {
    
    // Checks if two entities are currently overlapping based on their collision components and positions.
    public boolean overlaps(ICollidable a, ICollidable b) {
        if (a.getCollisionComponent() == null || b.getCollisionComponent() == null) {
            return false;
        }
        
        Rectangle boundsA = a.getCollisionComponent().getBounds(a.getPosition());
        Rectangle boundsB = b.getCollisionComponent().getBounds(b.getPosition());
        
        return boundsA.overlaps(boundsB);
    }
}