package io.github.cybergrid.simulation.engine.inputoutput;

import com.badlogic.gdx.InputProcessor;

import io.github.cybergrid.simulation.engine.managers.InputOutputManager;

public class TouchProcessor implements InputProcessor { 
    private InputOutputManager manager;

    public TouchProcessor(InputOutputManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        manager.receivedTouchInput(button, true);
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        manager.receivedTouchInput(button, false);
        return true;
    }

    @Override
    public boolean keyDown(int keycode) { return false; }

    @Override
    public boolean keyUp(int keycode) { return false; }

    @Override
    public boolean keyTyped(char character) { return false; }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }

    @Override
    public boolean mouseMoved(int screenX, int screenY) { return false; }

    @Override
    public boolean scrolled(float amountX, float amountY) { return false; }
    
    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
}
