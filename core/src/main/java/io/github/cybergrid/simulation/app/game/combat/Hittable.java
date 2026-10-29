package io.github.cybergrid.simulation.app.game.combat;

import com.badlogic.gdx.math.Vector2;

/** Implemented by any entity that can be struck by a bullet. */
public interface Hittable {
    void onHitByBullet(Vector2 bulletDirection);
}
