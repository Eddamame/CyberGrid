package io.github.cybergrid.simulation.engine.managers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.cybergrid.simulation.engine.collision.CollisionDetector;
import io.github.cybergrid.simulation.engine.collision.CollisionHandler;
import io.github.cybergrid.simulation.engine.collision.SpatialHashGrid;
import io.github.cybergrid.simulation.engine.entities.Entity;
import io.github.cybergrid.simulation.engine.events.CollisionEvent;
import io.github.cybergrid.simulation.engine.interfaces.ICollidable;

public class CollisionManager {

    private final CollisionDetector detector;
    private final CollisionHandler handler;
    private final SpatialHashGrid grid;
    private final EntityManager entityManager;
    private final EventManager eventManager;

    private final Set<String> activeContacts = new HashSet<>();
    private final Set<String> checkedPairsThisFrame = new HashSet<>();

    public CollisionManager(EntityManager entityManager, 
                            EventManager eventManager, 
                            CollisionDetector detector, 
                            CollisionHandler handler, 
                            SpatialHashGrid grid) {
        this.detector = detector;
        this.handler = handler;
        this.grid = grid;
        this.entityManager = entityManager;
        this.eventManager = eventManager;
    }

    public void reset() {
        activeContacts.clear();
        grid.clear();
    }

    public void update() {
        List<Entity> all = entityManager.getAllEntities();

        grid.clear();
        checkedPairsThisFrame.clear();

        for (Entity e : all) {
            if (e instanceof ICollidable) {
                grid.insert((ICollidable) e);
            }
        }

        for (Entity e : all) {
            if (!(e instanceof ICollidable)) continue;
            
            ICollidable a = (ICollidable) e;
            Set<ICollidable> candidates = grid.getPotentialColliders(a);

            for (ICollidable b : candidates) {
                String pairKey = Math.min(a.getID(), b.getID()) + "-" + Math.max(a.getID(), b.getID());

                if (!checkedPairsThisFrame.contains(pairKey)) {
                    checkedPairsThisFrame.add(pairKey);
                    processPair(a, b);
                }
            }
        }
    }

    private void processPair(ICollidable a, ICollidable b) {
        boolean overlapping = detector.overlaps(a, b);
        String contactKey = Math.min(a.getID(), b.getID()) + "-" + Math.max(a.getID(), b.getID());

        if (overlapping) {
            if (!activeContacts.contains(contactKey)) {
                activeContacts.add(contactKey);

                eventManager.dispatch(new CollisionEvent((Entity) a, (Entity) b));
                a.onCollision((Entity) b);
                b.onCollision((Entity) a);
            }

            if (a.getCollisionComponent() == null || b.getCollisionComponent() == null) {
                return; 
            }
            handler.resolvePhysics(a, b);

        } else {
            if (activeContacts.contains(contactKey)) {
                activeContacts.remove(contactKey);
                a.onCollisionExit((Entity) b);
                b.onCollisionExit((Entity) a);
            }
        }
    }
}
