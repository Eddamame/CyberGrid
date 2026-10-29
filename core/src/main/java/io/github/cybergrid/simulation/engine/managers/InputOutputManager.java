package io.github.cybergrid.simulation.engine.managers;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import io.github.cybergrid.simulation.engine.events.ScenePauseEvent;
import io.github.cybergrid.simulation.engine.events.SceneResumeEvent;
import io.github.cybergrid.simulation.engine.inputoutput.InputAction;
import io.github.cybergrid.simulation.engine.inputoutput.InputMapper;
import io.github.cybergrid.simulation.engine.inputoutput.KeyboardProcessor;
import io.github.cybergrid.simulation.engine.inputoutput.PointerProcessor;
import io.github.cybergrid.simulation.engine.inputoutput.TouchProcessor;
import io.github.cybergrid.simulation.engine.interfaces.IEventListener;

public class InputOutputManager {

    private Map<InputAction, Boolean> activeCommands;
    private Map<InputAction, Boolean> justPressedCommands;
    private InputMapper mapper;
    private InputMultiplexer multiplexer;
    private final Vector2 pointerPosition;
    private float scrollYDelta;
    private final EventManager eventManager;
    private final IEventListener<ScenePauseEvent> pauseListener = event -> clearInputStates(event.getClass().getSimpleName());
    private final IEventListener<SceneResumeEvent> resumeListener = event -> clearInputStates(event.getClass().getSimpleName());

    public InputOutputManager(EventManager eventManager) {
        this.activeCommands = new HashMap<>();
        this.mapper = new InputMapper();
        this.justPressedCommands = new HashMap<>();
        this.pointerPosition = new Vector2();
        this.scrollYDelta = 0f;
        this.eventManager = eventManager;
        setupProcessors();

        eventManager.subscribe(ScenePauseEvent.class, pauseListener);
        eventManager.subscribe(SceneResumeEvent.class, resumeListener);
    }

    private void setupProcessors() {
        PointerProcessor pointerProc = new PointerProcessor(this);
        KeyboardProcessor keyProc = new KeyboardProcessor(this);
        TouchProcessor touchProc = new TouchProcessor(this);

        multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(pointerProc);
        multiplexer.addProcessor(keyProc);
        multiplexer.addProcessor(touchProc);
    }

    public void bindKey(int keycode, InputAction command) {
        mapper.bindKey(keycode, command);
    }

    public void bindTouch(int buttonCode, InputAction command) {
        mapper.bindTouch(buttonCode, command);
    }

    public InputProcessor getInputProcessor() {
        return multiplexer;
    }

    public boolean isCommandActive(InputAction command) {
        return activeCommands.getOrDefault(command, false);
    }

    public boolean isCommandJustPressed(InputAction command) {
        return justPressedCommands.getOrDefault(command, false);
    }

    public Vector2 getPointerPosition() {
        return new Vector2(pointerPosition);
    }

    public Vector2 getPointerPosition(Vector2 out) {
        return out.set(pointerPosition);
    }

    public void update() {
        justPressedCommands.clear();
        scrollYDelta = 0f;
    }

    public void receiveKeyInput(int keycode, boolean isPressed) {
        InputAction command = mapper.getCommand(keycode);
        if (command != null) {
            activeCommands.put(command, isPressed);
            if (isPressed) {
                justPressedCommands.put(command, true);
            }
        }
    }

    public void receivedTouchInput(int buttonCode, boolean isPressed) {
        InputAction command = mapper.getButtonCommand(buttonCode);

        if (command != null) {
            activeCommands.put(command, isPressed);
            if (isPressed) {
                justPressedCommands.put(command, true);
            }
        }
    }

    public void receivePointerInput(int screenX, int screenY) {
        pointerPosition.set(screenX, screenY);
    }

    public void receiveScrollInput(float amountY) {
        scrollYDelta += amountY;
    }

    public float consumeScrollYDelta() {
        float delta = scrollYDelta;
        scrollYDelta = 0f;
        return delta;
    }


    public void resetInputStates() {
        activeCommands.clear();
        justPressedCommands.clear();
        scrollYDelta = 0f;
    }

    private void clearInputStates(String reason) {
        resetInputStates();
    }

    public void dispose() {
        eventManager.unsubscribe(ScenePauseEvent.class, pauseListener);
        eventManager.unsubscribe(SceneResumeEvent.class, resumeListener);
    }
}
