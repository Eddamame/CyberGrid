package io.github.cybergrid.simulation.app.game.input;

import com.badlogic.gdx.math.Vector2;

import io.github.cybergrid.simulation.engine.managers.InputOutputManager;

public final class GameCursorInput {

    private GameCursorInput() {
    }

    public static Vector2 getDirection(
        InputOutputManager ioManager,
        GameCommand up,
        GameCommand down,
        GameCommand left,
        GameCommand right
    ) {
        Vector2 direction = new Vector2();

        if (ioManager.isCommandActive(up)) {
            direction.y += 1f;
        }
        if (ioManager.isCommandActive(down)) {
            direction.y -= 1f;
        }
        if (ioManager.isCommandActive(left)) {
            direction.x -= 1f;
        }
        if (ioManager.isCommandActive(right)) {
            direction.x += 1f;
        }

        if (direction.len2() > 0f) {
            direction.nor();
        }

        return direction;
    }
}
