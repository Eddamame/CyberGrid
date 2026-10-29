package io.github.cybergrid.simulation.engine.collision;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import io.github.cybergrid.simulation.engine.interfaces.ICollidable;
import io.github.cybergrid.simulation.engine.managers.MovementManager;
import io.github.cybergrid.simulation.engine.movement.MovementComponent;

public class CollisionHandler {

    private final MovementManager movementManager;

    public CollisionHandler(MovementManager movementManager) {
        this.movementManager = movementManager;
    }

    public void resolvePhysics(ICollidable a, ICollidable b) {
        CollisionComponent compA = a.getCollisionComponent();
        CollisionComponent compB = b.getCollisionComponent();

        if (compA == null || compB == null || !compA.isSolid() || !compB.isSolid()) {
            return;
        }

        Rectangle rectA = compA.getBounds(a.getPosition());
        Rectangle rectB = compB.getBounds(b.getPosition());

        float overlapX = Math.min(rectA.x + rectA.width, rectB.x + rectB.width) - Math.max(rectA.x, rectB.x);
        float overlapY = Math.min(rectA.y + rectA.height, rectB.y + rectB.height) - Math.max(rectA.y, rectB.y);

        if (overlapX > 0 && overlapY > 0) {
            Vector2 pushA = new Vector2(0, 0);
            Vector2 pushB = new Vector2(0, 0);

            if (overlapX < overlapY) {
                float signX = Math.signum(a.getPosition().x - b.getPosition().x);
                if (signX == 0) signX = 1;
                pushA.x = overlapX * signX;
                pushB.x = -pushA.x;
            } else {
                float signY = Math.signum(a.getPosition().y - b.getPosition().y);
                if (signY == 0) signY = 1;
                pushA.y = overlapY * signY;
                pushB.y = -pushA.y;
            }

            applyBoxResolution(a, compA, pushA);
            applyBoxResolution(b, compB, pushB);
        }
    }

    private void applyBoxResolution(ICollidable e, CollisionComponent comp, Vector2 push) {
        if (comp.isStatic() || push.isZero()) return;

        movementManager.displacePosition(e.getID(), push);

        MovementComponent mc = movementManager.getComponent(e.getID());
        if (mc != null) {
            if (push.x != 0) mc.getVelocity().x = 0;
            if (push.y != 0) mc.getVelocity().y = 0;
        }

        if (comp.getBounceForce() > 0) {
            movementManager.applyImpulse(e.getID(), push.cpy().nor().scl(comp.getBounceForce()));
        }
    }
}