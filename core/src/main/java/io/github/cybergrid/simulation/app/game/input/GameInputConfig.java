package io.github.cybergrid.simulation.app.game.input;

import com.badlogic.gdx.Input;

import io.github.cybergrid.simulation.engine.managers.InputOutputManager;

public class GameInputConfig {
    public static void setupDefaultControls(InputOutputManager ioManager) {
        ioManager.bindKey(Input.Keys.W, GameCommand.MOVE_UP);
        ioManager.bindKey(Input.Keys.S, GameCommand.MOVE_DOWN);
        ioManager.bindKey(Input.Keys.A, GameCommand.MOVE_LEFT);
        ioManager.bindKey(Input.Keys.D, GameCommand.MOVE_RIGHT);
        ioManager.bindKey(Input.Keys.SPACE, GameCommand.OPEN_SETTINGS);
        ioManager.bindKey(Input.Keys.M, GameCommand.TOGGLE_MISSION);
        ioManager.bindKey(Input.Keys.H, GameCommand.TOGGLE_HELP);
        ioManager.bindKey(Input.Keys.ENTER, GameCommand.INTERACT);
        ioManager.bindTouch(Input.Buttons.LEFT, GameCommand.CLICK);
    }
}
