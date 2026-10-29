package io.github.cybergrid.simulation.engine.inputoutput;

import com.badlogic.gdx.InputProcessor;

import io.github.cybergrid.simulation.engine.managers.InputOutputManager;

public class KeyboardProcessor implements InputProcessor {
    private InputOutputManager manager;

    public KeyboardProcessor(InputOutputManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean keyDown(int keycode) {
        manager.receiveKeyInput(keycode, true); 
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        manager.receiveKeyInput(keycode, false); 
        return true;
    }
    
    @Override
    public boolean keyTyped(char character) { return false; }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }

    @Override
    public boolean mouseMoved(int screenX, int screenY) { return false; }

    @Override
    public boolean scrolled(float amountX, float amountY) { return false; }
    
    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
}
