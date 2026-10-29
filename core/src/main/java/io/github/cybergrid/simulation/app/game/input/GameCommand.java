package io.github.cybergrid.simulation.app.game.input;

import io.github.cybergrid.simulation.engine.inputoutput.InputAction;

public enum GameCommand implements InputAction {
    MOVE_UP, 
    MOVE_DOWN, 
    MOVE_LEFT, 
    MOVE_RIGHT,
    CLOSE,
    OPEN_SETTINGS,
    TOGGLE_MISSION,
    TOGGLE_HELP,
    INTERACT,
    CLICK;
}
