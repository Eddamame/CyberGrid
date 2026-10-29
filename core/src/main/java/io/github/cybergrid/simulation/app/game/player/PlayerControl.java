package io.github.cybergrid.simulation.app.game.player;

import com.badlogic.gdx.math.Vector2;

import io.github.cybergrid.simulation.app.game.input.GameCommand;
import io.github.cybergrid.simulation.app.game.input.GameCursorInput;
import io.github.cybergrid.simulation.engine.managers.InputOutputManager;
import io.github.cybergrid.simulation.engine.managers.MovementManager;

public class PlayerControl {
    
    private final InputOutputManager ioManager;
    private final MovementManager movementManager;
    private final int playerId;
    private static final float FORCE_MAGNITUDE = 5000f;
    private String lastMovementDirection = "IDLE";

    public PlayerControl(InputOutputManager ioManager, MovementManager movementManager, int playerId) {
        this.ioManager = ioManager;
        this.movementManager = movementManager;
        this.playerId = playerId;
    }

    public void update() {
        Vector2 direction = GameCursorInput.getDirection(
            ioManager,
            GameCommand.MOVE_UP,
            GameCommand.MOVE_DOWN,
            GameCommand.MOVE_LEFT,
            GameCommand.MOVE_RIGHT
        );

        logMovementDirection(direction);

        Vector2 force = direction.scl(FORCE_MAGNITUDE);

        if (!force.isZero()) {
            movementManager.applyForce(playerId, force);
        }
    }

    private void logMovementDirection(Vector2 direction) {
        String currentDirection = describeDirection(direction);
        if (currentDirection.equals(lastMovementDirection)) {
            return;
        }

        System.out.println("[Player Control] Player " + playerId + " movement: " + currentDirection);
        lastMovementDirection = currentDirection;
    }

    private String describeDirection(Vector2 direction) {
        if (direction.isZero()) {
            return "IDLE";
        }

        StringBuilder builder = new StringBuilder();
        if (direction.y > 0f) {
            builder.append("UP");
        } else if (direction.y < 0f) {
            builder.append("DOWN");
        }

        if (direction.x > 0f) {
            if (builder.length() > 0) {
                builder.append("_");
            }
            builder.append("RIGHT");
        } else if (direction.x < 0f) {
            if (builder.length() > 0) {
                builder.append("_");
            }
            builder.append("LEFT");
        }

        return builder.toString();
    }
}
