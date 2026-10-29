package io.github.cybergrid.simulation.engine.inputoutput;

import com.badlogic.gdx.InputProcessor;

import io.github.cybergrid.simulation.engine.managers.InputOutputManager;

public class PointerProcessor implements InputProcessor {
    private InputOutputManager manager;

    public PointerProcessor(InputOutputManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean keyDown(int keycode) { return false; }

    @Override
    public boolean keyUp(int keycode) { return false; }

    @Override
    public boolean keyTyped(char character) { return false; }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        manager.receivePointerInput(screenX, screenY);
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        manager.receivePointerInput(screenX, screenY);
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        manager.receivePointerInput(screenX, screenY);
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        manager.receivePointerInput(screenX, screenY);
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        manager.receivePointerInput(screenX, screenY);
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        manager.receiveScrollInput(amountY);
        return true;
    }
}
