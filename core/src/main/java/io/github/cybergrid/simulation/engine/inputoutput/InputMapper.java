package io.github.cybergrid.simulation.engine.inputoutput;

import java.util.HashMap;
import java.util.Map;

public class InputMapper {

    private Map<Integer, InputAction> keyMapping;
    private Map<Integer, InputAction> buttonMapping;

    public InputMapper() {
        keyMapping = new HashMap<>();
        buttonMapping = new HashMap<>();
    }

    public InputAction getCommand(int keycode) {
        return keyMapping.get(keycode);
    }

    public void bindKey(int keycode, InputAction action) {
        keyMapping.put(keycode, action);
    }

    public void bindTouch(int buttonCode, InputAction action) {
        buttonMapping.put(buttonCode, action);
    }
    public InputAction getButtonCommand(int buttonCode) {
        return buttonMapping.get(buttonCode);
}
}
